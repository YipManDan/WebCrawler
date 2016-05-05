import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A web crawler created for our COEN 272 class.
 * The main class which initializes the web crawling.
 * Contains the spider class which handles the bulk of the crawling work.
 *
 * @author  Daniel Yeh and Jesse Harder
 * @version 1.0
 * @since   2016-04-13
 */
public class Crawler {
    /**
     * The spider class that the Crawler class uses to perform the actual web crawling.
     *
     * @author  Daniel Yeh and Jesse Harder
     * @version 1.0
     * @since   2016-04-13
     */
    class Spider implements Runnable {
        public int spiderID;
        public Thread spiderThread;
        public int sleepTime = 3000;

        private int defaultCrawlDelay = 5000;

        /**
         * Constructor for Spider. Handles creation of a spider thread, outputs a message then returns to caller.
         * @param ID    ID number for the spider thread.
         */
        Spider(int ID) {
            spiderID = ID;
            spiderThread = new Thread(this, "Spider Thread " + String.valueOf(spiderID));
        }

        /**
         * The main functionality of the Spider goes here.
         */
        @Override
        public void run() {
            URL urlToCrawl; //Current URL the spider intends to crawl
            System.out.println("Spider " + spiderID + " is crawling.");

            boolean hasSlept = false;
            boolean keepCrawling = true;
            int numPagesCrawledDuringCheck;

            /**
             * This loop handles all the main functionality of the spider thread and continues until a break condition.
             * The loop will:
             *  Obtain an URL and determine whether or not it is to be crawled at this point/or ever.
             *  Download and store information.
             * The loop ends when there appears to be no more links to crawl or the limit to crawl has been reached.
             */
            while (keepCrawling) {
                //Get URL to crawl from queue
                urlToCrawl = URLs_to_crawl.poll();

                //Reset boolean flag
                boolean needToSleep = false;

                //Atomic operation to get current value of pages crawled and increment the counter
                numPagesCrawledDuringCheck = numPagesCrawled.getValAndIncrement();

                // If the queue was empty or the crawl limit has been reached.
                // Need to either sleep or kill self.
                if (urlToCrawl == null) {
                    needToSleep = true;
                    numPagesCrawled.decrement();
                }
                else if (numPagesCrawledDuringCheck >= numberOfPagesToCrawl()) {
                    needToSleep = true;
                    numPagesCrawled.decrement();
                }

                if (needToSleep) {
                    // Already tried once and slept.
                    // Thread believes: Queue is empty or number of pages to be crawled reached.
                    if (numberOfSpiders == 1 || hasSlept)
                        break;
                    //Sleep the thread
                    else
                    {
                        hasSlept = true;
                        try { Thread.sleep(sleepTime); }
                        catch (InterruptedException e) {
                            System.err.println("InterruptException when Spider "+spiderID+" was sleeping.");
                        }

                        // Wake up and try again.
                        continue;
                    }
                }

                // We now know we have a URL.
                hasSlept = false;

                // Check if the URL to crawl has already been crawled.
                if (URLs_not_to_crawl.contains(urlToCrawl.toString())) {
                    numPagesCrawled.decrement();
                    continue;
                }

                // If the url we are about to crawl does not match the URL restriction, do not crawl it.
                if (URLRestriction() != null && !urlToCrawl.toString().startsWith(URLRestriction().toString())) {
                    //TODO: Resolve the following
                    // Could do this: URLs_not_to_crawl.add(urlToCrawl.toString());
                    // I think that is slower though than just doing the string compare.
                    numPagesCrawled.decrement();
                    continue;
                }

                //Create a reference to a RobotsChecker class which handles download and storage of robots.txt restrictions
                RobotsChecker robotsChecker;

                // Check if we have already got this robot
                if (robots.containsKey(urlToCrawl.getHost())) {
                    //robot has already been created
                    robotsChecker = robots.get(urlToCrawl.getHost());
                } else {
                    // We have not gotten this robot before.
                    robotsChecker = new RobotsChecker(urlToCrawl);
                    robots.put(urlToCrawl.getHost(),robotsChecker);
                }

                // Check the robots.txt file to see if the host disallows this URL to be accessed.
                if(!robotsChecker.isAllowed(urlToCrawl)) {
                    //This host has disallowed access to this URL
                    //Continue on without accessing the page
                    numPagesCrawled.decrement();
                    continue;
                }

                //synchronized lock, the prevents multiple threads from getting simultaneous positive
                //response to this check
                synchronized (lock) {
                    // Check to see if this host has been accessed recently.
                    if (recentlyAccessedURLHosts.contains(urlToCrawl.getHost())) {
                        // This host has been accessed recently.
                        // Place back in queue to wait till later and try a different URL.
                        URLs_to_crawl.add(urlToCrawl);
                        numPagesCrawled.decrement();
                        continue;
                    }
                    // Add the host of the URL we are about to access to this list
                    // This prevents multiple threads from accessing a new host simultaneously
                    recentlyAccessedURLHosts.add(urlToCrawl.getHost());
                }

                /**
                 * Inner class extending TimerTask in order to remove elements from the list of recently accessed URLs.
                 */
                class Remover_Task extends TimerTask {
                    String urlHostToRemove;
                    public void setHostToRemove(String urlHost) {urlHostToRemove = urlHost;}

                    @Override public void run() {
                        recentlyAccessedURLHosts.remove(urlHostToRemove);
                    }
                }

                // Schedule a timer to remove that element from the list after a delay
                // so that we can eventually go back to that host.

                Timer t = new Timer();
                Remover_Task removerTask = new Remover_Task();
                removerTask.setHostToRemove(urlToCrawl.getHost());

                //Check if the host's robot.txt page specifies a crawl-delay
                if (robotsChecker.crawlDelay == -1)
                    t.schedule(removerTask, defaultCrawlDelay);
                else {
                    t.schedule(removerTask, robotsChecker.crawlDelay * 1000);
                    System.out.println("Timer has an updated crawl delay: " + robotsChecker.crawlDelay*1000 + "milliseconds");
                }

                /* At this point we are ready to get the page. */

                //Open document
                try {
                    //Open a connection to the page, get a response and obtain the document and status code from the response
                    Connection connection = Jsoup.connect(urlToCrawl.toString()).userAgent("Mozilla");
                    Connection.Response response = connection.timeout(5000).execute();
                    String contentType = response.contentType();
                    if(!contentType.contains("text/html")) {
                        System.out.println("Page downloaded was not of content type: text/html");
                        numPagesCrawled.decrement();
                        continue;
                    }
                    Document doc = Jsoup.parse(response.body());
                    int statusCode = response.statusCode();

                    String title = doc.title();

                    //Store all images and links in an ArrayList
                    Elements images = doc.select("img[src]");
                    Elements links = doc.select("a");

                    //Clean downloaded document with Jsoup Cleaner. Removes images and head.
                    Whitelist whitelist = Whitelist.relaxed();
                    whitelist.addTags("all");
                    whitelist.removeTags("img");

                    Cleaner cleaner = new Cleaner(whitelist);
                    doc = cleaner.clean(doc);

                    //Reinsert title into a document
                    doc.title(title);
                    if(title.length() == 0) {
                        title = "Untitled";
                    }

                    //Remove all image and references to images from a document
                    images.remove();

                    //Output title of the page
                    System.out.println("");
                    System.out.println("Spider " + spiderID + " downloaded: " + title.toString() + " " + urlToCrawl);

                    //Store and count links in document
                    int linkCount = 0;
                    for(Element link: links) {

                        //Obtain both relative and absolute links
                        String linkString = link.attr("href");
                        String absLinkString = link.attr("abs:href");

                        if(linkString.length() == 0)
                            continue;

                        String finalLinkString;

                        // Possibly handle the case of a relative link.
                        if (linkString.equals(absLinkString))
                            // Link was not relative. Use as is.
                            finalLinkString = linkString;
                        else {
                            finalLinkString = urlToCrawl.getProtocol() + "://" + urlToCrawl.getHost() + linkString;
                        }

                        //Add valid links to queue of URLs to crawl
                        try {
                            URL urlToQueue = new URL(finalLinkString);
                            linkCount++;
                            URLs_to_crawl.add(urlToQueue);
                        } catch (MalformedURLException e) {
                            System.err.println("Spider.run got malformed URL exception for a link found on page.\n" +
                                    "Link will not be added to the queue");
                            System.err.println("Link: " + linkString);
                            System.err.println("Abs Link: " + absLinkString);
                            System.err.println("Final Link: " + finalLinkString);
                        }

                    }

                    // Add current URL to list, to prevent crawler from re-crawling a page
                    URLs_not_to_crawl.add(urlToCrawl.toString());

                    // Determine name for the file.
                    String filename = urlToCrawl.getHost().toString() + ".html";

                    //Loop to prevent a file overwriting a previously saved file
                    int nameAttemptCounter = 1;
                    while (fileNamesUsed.contains(outputPath + filename)) {
                        filename = urlToCrawl.getHost().toString() + nameAttemptCounter + ".html";
                        nameAttemptCounter++;
                    }
                    fileNamesUsed.add(outputPath + filename);
                    System.out.println("Saving file: " + filename);

                    // Write document information to report.html
                    try{
                        tableRowNumber.increment();
                        bufferedWriter.write("\t<tr>\n\t\t<td>" + tableRowNumber.val() + "</td>\n" + "" +
                                "\t\t<td><a href=\"" + urlToCrawl + "\">" + title + "</a></td>\n" +
                                "\t\t<td><a href=\"" + outputPath + filename + "\">" + filename + "</a></td>\n" +
                                "\t\t<td>" + statusCode + "</td>\n" +
                                "\t\t<td>" + linkCount + "</td>\n" +
                                "\t\t<td>" + images.size() + "</td>\n" +
                                "\t</tr>\n");
                    }
                    catch (IOException e){
                        System.err.println("IOException writing to HTML file: " + title + " " + e.getMessage());
                    }

                    // Write document out to file with the determined filename
                    Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath + filename), "UTF-8"));
                    try{
                        out.write(doc.outerHtml());
                    }
                    finally {
                        out.close();
                    }
                }
                catch (IOException e){
                    System.err.println("IOException for Spider " + spiderID + ": " + e.toString());
                    numPagesCrawled.decrement();
                }

                System.out.println("Number of pages crawled: "+numPagesCrawled);
            }

            System.out.println("Spider " + spiderID + " has stopped crawling.");
            return;
        }
    }

    /* ---------- Start Crawler stuff here. ---------- */

    // Data
    protected File csvFile;
    protected String outputPath;
    protected int numberOfSpiders = 5;
    // Left for future distributed crawler scaling
    //    protected int numberOfQueues;

    //Thread safe integers which allow for concurrent counters
    private ThreadSafeInt numPagesCrawled;
    private ThreadSafeInt tableRowNumber;

    private Set<String> recentlyAccessedURLHosts;   // For the URLs that should not be crawled again yet.
    private Queue<URL> URLs_to_crawl;           // For the URLs scraped and queued but not yet crawled
    private Set<String> URLs_not_to_crawl;          // For the URLs already crawled.
    private Set<String> fileNamesUsed;          // For the filenames already assigned
    private ConcurrentHashMap<String, RobotsChecker> robots;        //HashMap of created RobotSChecker classes

    // Contained Classes
    private FileInterface fileInterface;
    private TextAreaLogProgram textAreaLogProgram;
    private CSV_Parser csvParser;
    private List<Spider> spiders;
    protected BufferedWriter bufferedWriter;

    // The lock.
    private Object lock;

    /**
     * Constructor method for Crawler class
     */
    Crawler () {

        numPagesCrawled = new ThreadSafeInt(0);
        tableRowNumber = new ThreadSafeInt(0);

        recentlyAccessedURLHosts = new ConcurrentSkipListSet<String>();
        URLs_to_crawl = new ConcurrentLinkedQueue<URL>();
        URLs_not_to_crawl = new ConcurrentSkipListSet<String>();
        fileNamesUsed = new ConcurrentSkipListSet<String>();
        robots = new ConcurrentHashMap<String, RobotsChecker>();


        // Creates a new fileInterface class to interact with user
        fileInterface = new FileInterface(this);

        // Creates a lock object to be used for thread-safety
        lock = new Object();
    }

    /**
     * Method called by FileInterface to begin the crawling
     */
    public void startCrawl() {
        // Section of code attempts to create a JFrame that would show log outputs, but it doesn't end the program when closed.
//        textAreaLogProgram = new TextAreaLogProgram(this);
//        textAreaLogProgram.thread.start();
//
//        System.setOut(textAreaLogProgram.getPrintStream());
//        System.setErr(textAreaLogProgram.getPrintStream());


        System.out.println("Starting crawl.");
        // Dispose of the interface now that we're done with it..
        if (fileInterface != null) fileInterface.dispose();

        // Parse the CSV file.
        csvParser = new CSV_Parser();
        csvParser.parseFile(fileInterface.getFileChosen());

        // Add the seed URL to the list of URLs that need crawling.
        URLs_to_crawl.add(seedURL());

        //Create report.html
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputPath + "report.html"));
            bufferedWriter.write("<!doctype html>\n<html>\n<head>\n\t<title>Report</title>\n</head>\n");
            bufferedWriter.write("<body>\n\t<table border=\"1\">\n\t<tr>\n\t\t<th>Item #</th>\n\t\t<th>Title</th>\n\t\t<th>Document Location</th>\n\t\t<th>HTTP Status Code</th>\n" +
                    "\t\t<th>Number of Outlinks</th>\n\t\t<th>Number of Images</th>\n\t</tr>\n");
        }
        catch (IOException e){
            System.err.println("IOException creating bufferedWriter" + e.getMessage());
        }

        spiders = new ArrayList<Spider>();
        // Create Spiders.
        for (int i = 0; i < numberOfSpiders; i++) {
            Spider spider = new Spider(i);
            System.out.println("Spawning spider " + i);
            spiders.add(spider);
        }

        while (true) {
            // Start the spiders crawling.
            for (Spider spider : spiders) {
                spider.spiderThread.start();
            }

            //Wait for all of the spiders to finish their work.
            for (int i = 0; i < numberOfSpiders; i++) {
                try {
                    spiders.get(i).spiderThread.join();
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException thrown in Crawler.startCrawl when trying to join spider threads.");
                    e.printStackTrace();
                }
            }

            // Check to see that we did crawl the right number of pages.
            if (numPagesCrawled.val() >= numberOfPagesToCrawl()) {
                System.out.println("Crawler finished. All pages downloaded successfully.");
                break;
            }

            // Check whether we got too few pages because we ran out of links.
            if (URLs_to_crawl.isEmpty()) {
                System.out.println("Crawler finished. Less than requested pages downloaded because the spiders ran out of links.");
                break;
            }

            // There are still URLs to crawl and we don't have enough pages, so keep running.
        }


        //End html file
        try {
            bufferedWriter.write("</table>\n</body>\n</html>");
            bufferedWriter.close();
        }
        catch (IOException e){
            System.err.println("IOException at end of html file: " + e.getMessage());
        }

        // End code execution here.
        System.exit(0);
    }

    /**
     * To be called to prematurely end program.
     * Typical call is the event the user closes the FileInterface window
     */
    protected void endProgram(){
        //System.setOut(textAreaLogProgram.getStandardOut());
        System.out.println("Closing Sequence");
        System.exit(1);
    }

    /* Shorthand functions for accessing elements of csvParser */

    // CSV Parser
    private URL seedURL() {
        return csvParser.seedURL;
    }
    private int numberOfPagesToCrawl() {
        return csvParser.numberOfPagesToCrawl;
    }
    private URL URLRestriction() {
        return csvParser.URLRestriction;
    }

    public static void main(String[] args){
        new Crawler();
    }
}
