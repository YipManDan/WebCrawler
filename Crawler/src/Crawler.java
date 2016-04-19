import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.*;
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

        Spider(int ID) {
            spiderID = ID;
            spiderThread = new Thread(this, "Spider Thread " + String.valueOf(spiderID));
        }

        public void crawl() {
            spiderThread.start();
        }

        /**
         * The main functionality of the Spider goes here.
         */
        @Override
        public void run() {
            URL urlToCrawl;
            System.out.println("Spider " + spiderID + " is crawling.");

            boolean hasSlept = false;

            while (numPagesCrawled.val() < numberOfPagesToCrawl()) {
                //Get URL to crawl from queue
                urlToCrawl = URLs_to_crawl.poll();

                // If the queue was empty, either kill self or sleep.
                if (urlToCrawl == null) {
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
                else if (URLRestriction() != null && !urlToCrawl.toString().contains(URLRestriction().toString())) {
                    // If the URL restriction is not within the url we are about to crawl, do not crawl it.
                    // Could do this: URLs_not_to_crawl.add(urlToCrawl.toString());
                    continue;
                }
                else if (recentlyAccessedURLHosts.contains(urlToCrawl.getHost())) {
                        // This host has been accessed recently.
                        // Place back in queue to wait till later and try a different URL.
                        URLs_to_crawl.add(urlToCrawl);
                        continue;
                }

                hasSlept = false;

                // Check if the URL to crawl has already been crawled.
                if (URLs_not_to_crawl.contains(urlToCrawl.toString()))
                    continue;

                //Open document
                try {
                    org.jsoup.nodes.Document doc = Jsoup.connect(urlToCrawl.toString()).userAgent("Mozilla").get();

                    String title = doc.title();

                    //Clean downloaded document with Jsoup Cleaner. Removes images.
                    Cleaner cleaner = new Cleaner(Whitelist.basic());
                    doc = cleaner.clean(doc);

                    //Output title of the page
                    System.out.println("Spider " + spiderID + " downloaded: " + urlToCrawl);
                    System.out.println("Spider " + spiderID + " downloaded: " + title.toString() + " " + urlToCrawl);

                    //Elements links = doc.select("a[href]");
                    Elements links = doc.select("a");

                    for(Element link: links) {
                        String linkString = link.attr("abs:href");
                        URL urlToQueue = new URL(linkString);
                        URLs_to_crawl.add(urlToQueue);
//                        System.out.println("link: " + linkString);
                    }

                    URLs_not_to_crawl.add(urlToCrawl.toString());
                    numPagesCrawled.increment();

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
                            System.out.println("Removing "+urlHostToRemove+".");
                            recentlyAccessedURLHosts.remove(urlHostToRemove);
                        }
                    }

                    // Schedule a timer to remove that element from the list after a delay
                    // so that we can eventually go back to that host.
                    int accessDelay = 5000;
                    Timer t = new Timer();
                    Remover_Task removerTask = new Remover_Task();
                    removerTask.setHostToRemove(urlToCrawl.getHost());
                    System.out.println("Scheduling timer to remove "+urlToCrawl.getHost()+".");
                    t.schedule(removerTask, accessDelay);

                    // Figure out a name for the file.
                    //String filename = outputPath+title+".html";
                    String filename = outputPath + urlToCrawl.getHost().toString() + ".html";
                    int nameAttemptCounter = 1;
                    while (fileNamesUsed.contains(filename)) {
                        //filename = outputPath+title+nameAttemptCounter+".html";
                        filename = outputPath + urlToCrawl.getHost().toString() + nameAttemptCounter + ".html";
                        nameAttemptCounter++;

                    }
                    fileNamesUsed.add(filename);
                    System.out.println("Saving file: " + filename);

                    Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
                    try{
                        out.write(doc.outerHtml());
                    }
                    finally {
                        out.close();
                    }
                }
                catch (IOException e){
                    System.err.println("IOException for Spider " + spiderID + ": " + e.getMessage());
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
    protected int numberOfSpiders = 1;
    //    protected int numberOfQueues;
    private ThreadSafeInt numPagesCrawled;
    private Set<String> recentlyAccessedURLHosts;   // For the URLs that should not be crawled again yet.
    private Queue<URL> URLs_to_crawl;           // For the URLs scraped and queued but not yet crawled
    private Set<String> URLs_not_to_crawl;          // For the URLs already crawled.
    private Set<String> fileNamesUsed;

    // Contained Classes
    private FileInterface fileInterface;
    private CSV_Parser csvParser;
    private List<Spider> spiders;

    Crawler () {
        numPagesCrawled = new ThreadSafeInt(0);
        recentlyAccessedURLHosts = new ConcurrentSkipListSet<String>();
        URLs_to_crawl = new ConcurrentLinkedQueue<URL>();
        URLs_not_to_crawl = new ConcurrentSkipListSet<String>();
        fileNamesUsed = new ConcurrentSkipListSet<String>();

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

        spiders = new ArrayList<Spider>();
        // Create Spiders and make them run.
        for (int i = 0; i < numberOfSpiders; i++) {
            Spider spider = new Spider(i);
            spiders.add(spider);
            spider.crawl();
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

        // Maybe do stuff with what the spiders gathered.
        System.out.println("Got to end of startCrawl.");
        // End code execution here.
        System.exit(0);
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
