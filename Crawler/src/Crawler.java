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
        };

        public void crawl() {
            spiderThread.start();
        }

        /**
         * The main functionalty of the Spider goes here.
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
                    if (numberOfSpiders == 1 || hasSlept)
                        break;
                    else
                    {
                        hasSlept = true;
                        try { Thread.sleep(sleepTime); }
                        catch (InterruptedException e) {
                            System.err.println("InterruptException when Spider "+spiderID+" was sleeping.");
                        }
                        continue;
                    }
                }

                hasSlept = false;

                // Check if the URL to crawl has already been crawled.
                if (URLs_crawled.contains(urlToCrawl.toString()))
                    continue;

                //Open document
                try {
                    org.jsoup.nodes.Document doc = Jsoup.connect(urlToCrawl.toString()).userAgent("Mozilla").get();

                    //Clean downloaded document with Jsoup Cleaner. Removes images.
                    Cleaner cleaner = new Cleaner(Whitelist.basic());
                    doc = cleaner.clean(doc);

                    //Output title of the page
                    String title = doc.title();
                    System.out.println("Spider " + spiderID + " downloaded: " + urlToCrawl);

                    //Elements links = doc.select("a[href]");
                    Elements links = doc.select("a");

                    for(Element link: links) {
                        String linkString = link.attr("abs:href");
                        URL urlToQueue = new URL(linkString);
                        URLs_to_crawl.add(urlToQueue);
                        System.out.println("link: " + linkString);
                    }

                    URLs_crawled.add(urlToCrawl.toString());
                    numPagesCrawled.increment();

                    /* Need to add top level of URL to list of top levels not to be
                     * queried and make a Timer to remove the URL at a later time.
                     */

                    // Figure out a name for the file.
                    String filename = title.toString()+".html";
                    int nameAttemptCounter = 1;
                    while (fileNamesUsed.contains(filename)) {
                        filename = title.toString()+nameAttemptCounter+".html";
                        nameAttemptCounter++;

                    }
                    fileNamesUsed.add(filename);

                    Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
                    try{
                        out.write(doc.outerHtml());
                    }
                    finally {
                        out.close();
                    }
                }
                catch (IOException e){
                    System.err.println("Spider " + spiderID + ": " + e.getMessage());
                    continue;
                }

            }

            System.out.println("Spider " + spiderID + " has stopped crawling.");
            return;
        }
    }

    /* ---------- Start Cralwer stuff here. ---------- */

    // Data
    protected File csvFile;
    protected int numberOfSpiders = 1;
    //    protected int numberOfQueues;
    private ThreadSafeInt numPagesCrawled;
    private Set<String> recentlyAccessedURLs;  // For the URLs that should not be crawled again yet.
    private Queue<URL> URLs_to_crawl;       // For the URLs scraped and queued but not yet crawled
    private Set<String> URLs_crawled;          // For the URLs already crawled.
    private Set<String> fileNamesUsed;

    // Contained Classes
    private FileInterface fileInterface;
    private CSV_Parser csvParser;
    private List<Spider> spiders;

    Crawler () {
        numPagesCrawled = new ThreadSafeInt(0);
        recentlyAccessedURLs = new ConcurrentSkipListSet<String>();
        URLs_to_crawl = new ConcurrentLinkedQueue<URL>();
        URLs_crawled = new ConcurrentSkipListSet<String>();
        fileNamesUsed = new ConcurrentSkipListSet<String>();

        fileInterface = new FileInterface(this);
    }

    public void startCrawl() {
        System.out.println("Starting crawl.");
        // Dispose of the interface now that we're done with it..
        if (fileInterface != null) fileInterface.dispose();

        // Parse the CSV file.
//        csvParser = new CSV_Parser();
//        csvParser.parseFile(fileInterface.getFileChosen());
        csvParser = new CSV_Parser("http://www.thesketchfellows.com/",5);

        // Add the seed URL to the list of URLs that need crawling.
        URLs_to_crawl.add(seedURL());

        spiders = new ArrayList<Spider>();
        // Create Spiders and make them run.
        for (int i = 0; i < numberOfSpiders; i++) {
            Spider spider = new Spider(i);
            spiders.add(spider);
            spider.crawl();
        }

        // Do stuff with what the spiders gathered.
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
