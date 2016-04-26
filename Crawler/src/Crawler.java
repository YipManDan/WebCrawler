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
 *
 * @author  Daniel Yeh and Jesse Harder
 * @version 1.0
 * @since   2016-04-13
 */
public class Crawler {
    /**
     * Exit message options for the spider.
     */
    enum CrawlExitMessage {
        NOT_SET,
        LIMIT_REACHED,
        EMPTY_QUEUE
    }

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
        public CrawlExitMessage exitMessage;

        private int defaultCrawlDelay = 5000;

        Spider(int ID) {
            spiderID = ID;
            spiderThread = new Thread(this, "Spider Thread " + String.valueOf(spiderID));
            exitMessage = CrawlExitMessage.NOT_SET;
        }

        /**
         * The main functionality of the Spider goes here.
         */
        @Override
        public void run() {
            URL urlToCrawl;
            System.out.println("Spider " + spiderID + " is crawling.");

            boolean hasSlept = false;
            boolean keepCrawling = true;

            while (keepCrawling) {
                //Get URL to crawl from queue
                urlToCrawl = URLs_to_crawl.poll();

                boolean needToSleep = false;
                exitMessage = CrawlExitMessage.NOT_SET;

                // If the queue was empty or the crawl limit has been reached.
                // Need to either sleep or kill self.
                if (urlToCrawl == null) {
                    needToSleep = true;
                    exitMessage = CrawlExitMessage.EMPTY_QUEUE;
                }
                else if (numPagesCrawled.val() >= numberOfPagesToCrawl()) {
                    needToSleep = true;
                    exitMessage = CrawlExitMessage.LIMIT_REACHED;
                }

                if (needToSleep) {
                    // Already tried once and slept. Queue is
                    if (numberOfSpiders == 1 || hasSlept)
                        break;
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
                numPagesCrawled.increment();

                // Check if the URL to crawl has already been crawled.
                if (URLs_not_to_crawl.contains(urlToCrawl.toString())) {
                    numPagesCrawled.decrement();
                    continue;
                }

                // If the url we are about to crawl does not match the URL restriction, do not crawl it.
                if (URLRestriction() != null && !urlToCrawl.toString().startsWith(URLRestriction().toString())) {
                    // Could do this: URLs_not_to_crawl.add(urlToCrawl.toString());
                    // I think that is slower though than just doing the string compare.
                    numPagesCrawled.decrement();
                    continue;
                }

                RobotsChecker robotsChecker;

                // Check if we have already got this robot
                if (robots.containsKey(urlToCrawl.getHost())) {
                    // We already got this robot.
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
                    System.out.println("URL is disallowed: " + urlToCrawl.toString());
                    numPagesCrawled.decrement();
                    continue;
                }

                // Check to see if this host has been accessed recently.
                if (recentlyAccessedURLHosts.contains(urlToCrawl.getHost())) {
                    // This host has been accessed recently.
                    // Place back in queue to wait till later and try a different URL.
                    URLs_to_crawl.add(urlToCrawl);
                    numPagesCrawled.decrement();
                    continue;
                }

//                //TODO: Decide if this belongs here
                // Added the host of the URL we just accessed to a list.
                // Use this list to see who not to access again soon.
                recentlyAccessedURLHosts.add(urlToCrawl.getHost());

                /**
                 * Inner class extending TimerTask in order to remove elements from the list of recently accessed URLs.
                 */
                class Remover_Task extends TimerTask {
                    String urlHostToRemove;
                    public void setHostToRemove(String urlHost) {urlHostToRemove = urlHost;}

                    @Override public void run() {
                        System.out.println("Removing "+urlHostToRemove+" from list of recently accessed hosts.");
                        recentlyAccessedURLHosts.remove(urlHostToRemove);
                    }
                }

                // Schedule a timer to remove that element from the list after a delay
                // so that we can eventually go back to that host.

                Timer t = new Timer();
                Remover_Task removerTask = new Remover_Task();
                removerTask.setHostToRemove(urlToCrawl.getHost());
                System.out.println("Scheduling timer to remove " + urlToCrawl.getHost() + ".");

                if (robotsChecker.crawlDelay == -1)
                    t.schedule(removerTask, defaultCrawlDelay);
                else {
                    t.schedule(removerTask, robotsChecker.crawlDelay * 1000);
                    System.out.println("Timer has an updated crawl delay: " + robotsChecker.crawlDelay*1000 + "sec");
                }

                /* At this point we are ready to get the page. */

                //Open document
                try {
                    Connection connection = Jsoup.connect(urlToCrawl.toString()).userAgent("Mozilla");
                    Connection.Response response = connection.timeout(5000).execute();
                    Document doc = Jsoup.parse(response.body());
                    int statusCode = response.statusCode();
//                    int statusCode = connection.timeout(5000).execute().statusCode();
//                    org.jsoup.nodes.Document doc = connection.get();



                    String title = doc.title();

                    //Get Links
                    Elements images = doc.select("img[src]");

                    //Clean downloaded document with Jsoup Cleaner. Removes images.
                    Whitelist whitelist = Whitelist.relaxed();
                    whitelist.addTags("all");
                    whitelist.removeTags("img");

                    Cleaner cleaner = new Cleaner(whitelist);
                    doc = cleaner.clean(doc);

                    doc.title(title);
                    if(title.length() == 0) {
                        title = "Untitled";
                    }

                    images.remove();

                    //Output title of the page
                    System.out.println("");
                    System.out.println("Spider " + spiderID + " downloaded: " + title.toString() + " " + urlToCrawl);

                    Elements links = doc.select("a");

                    for(Element link: links) {
                        String linkString = link.attr("abs:href");
//                        System.out.println("Saving URL: " + linkString);
                        if(linkString.length() == 0)
                            continue;
                        URL urlToQueue = new URL(linkString);
                        URLs_to_crawl.add(urlToQueue);
//                        System.out.println("link: " + linkString);
                    }

                    URLs_not_to_crawl.add(urlToCrawl.toString());

                    // Figure out a name for the file.
                    String filename = urlToCrawl.getHost().toString() + ".html";
                    int nameAttemptCounter = 1;
                    while (fileNamesUsed.contains(outputPath + filename)) {
                        filename = urlToCrawl.getHost().toString() + nameAttemptCounter + ".html";
                        nameAttemptCounter++;
                    }
                    fileNamesUsed.add(outputPath + filename);
                    System.out.println("Saving file: " + filename);

                    try{
                        tableRowNumber.increment();
                        bufferedWriter.write("<tr>\n\t\t<td>" + tableRowNumber.val() + "</td>\n" + "" +
                                "\t\t<td><a href=\"" + urlToCrawl + "\">" + title + "</a></td>\n" +
                                "\t\t<td><a href=\"" + outputPath + filename + "\">" + filename + "</a></td>\n" +
                                "\t\t<td>" + statusCode + "</td>\n" +
                                "\t\t<td>" + links.size() + "</td>\n" +
                                "\t\t<td>" + images.size() + "</td>\n" +
                                "\t</tr>\n");
                    }
                    catch (IOException e){
                        System.err.println("IOException writing to HTML file: " + title + " " + e.getMessage());
                    }

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
    //    protected int numberOfQueues;
    private ThreadSafeInt numPagesCrawled;
    private ThreadSafeInt tableRowNumber;
    private Set<String> recentlyAccessedURLHosts;   // For the URLs that should not be crawled again yet.
    private Queue<URL> URLs_to_crawl;           // For the URLs scraped and queued but not yet crawled
    private Set<String> URLs_not_to_crawl;          // For the URLs already crawled.
    private Set<String> fileNamesUsed;
    private ConcurrentHashMap<String, RobotsChecker> robots;

    // Contained Classes
    private FileInterface fileInterface;
    private CSV_Parser csvParser;
    private List<Spider> spiders;
    protected BufferedWriter bufferedWriter;

    Crawler () {
        numPagesCrawled = new ThreadSafeInt(0);
        tableRowNumber = new ThreadSafeInt(0);
        recentlyAccessedURLHosts = new ConcurrentSkipListSet<String>();
        URLs_to_crawl = new ConcurrentLinkedQueue<URL>();
        URLs_not_to_crawl = new ConcurrentSkipListSet<String>();
        fileNamesUsed = new ConcurrentSkipListSet<String>();
        robots = new ConcurrentHashMap<String, RobotsChecker>();

        fileInterface = new FileInterface(this);

    }

    public void startCrawl() {
        System.out.println("Starting crawl.");
        // Dispose of the interface now that we're done with it..
        if (fileInterface != null) fileInterface.dispose();

        // Parse the CSV file.
        csvParser = new CSV_Parser();
        csvParser.parseFile(fileInterface.getFileChosen());
        //csvParser = new CSV_Parser("http://www.thesketchfellows.com/",1,"http://www.thesketchfellows.com/");

        // Add the seed URL to the list of URLs that need crawling.
        URLs_to_crawl.add(seedURL());

        //Create report.html
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputPath + "report.html"));
            bufferedWriter.write("<!doctype html>\n<html>\n<head>\n\t<title>Report</title>\n</head>\n");
            bufferedWriter.write("<body>\n\t<table border=\"1\">\n\t<tr>\n\t\t<th>Item #</th>\n\t\t<th>Title</th>\n\t\t<th>Document Location</th>\n\t\t<th>HTTP Status Code</th>\n" +
                    "\t\t<th>Number of Outlinks</th>\n\t\t<th>Number of Images</th>\n\t</tr>");
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
//            spider.crawl();
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
        // Maybe do stuff with what the spiders gathered.
        System.out.println("Got to end of startCrawl.");
        // End code execution here.
        System.exit(0);
    }

    protected void endProgram(){
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
