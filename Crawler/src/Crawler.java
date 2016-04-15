import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.text.Document;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
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
        int spiderID;
        Thread spiderThread;

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
            URL url;
            System.out.println("Spider " + spiderID + " is crawling.");

            while (numPagesCrawled.val() < numberOfPagesToCrawl()) {
                // Put main body of spider functionality here.

                //Get URL to crawl from queue
                url = URLs_to_crawl.poll();


                //Open document
                try {
                    org.jsoup.nodes.Document doc = Jsoup.connect(url.toString()).get();

                    //Output title of the page
                    String title = doc.title();
                    System.out.println("Spider " + spiderID + "downloaded: " + title);

                    //Elements links = doc.select("a[href]");
                    Elements links = doc.select("a");

                    for(Element link: links) {
                        System.out.println("link: " + link.attr("abs:href"));
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
    private URL seedURL;
    private ThreadSafeInt numPagesCrawled;
    private Set<URL> recentlyAccessedURLs;  // For the URLs that should not be crawled again yet.
    private Queue<URL> URLs_to_crawl;       // For the URLs scraped and queued but not yet crawled
    private Set<URL> URLs_crawled;          // For the URLs already crawled.

    // Contained Classes
    private FileInterface fileInterface;
    private CSV_Parser csvParser;
    private List<Spider> spiders;

    Crawler () {
        numPagesCrawled = new ThreadSafeInt(0);
        recentlyAccessedURLs = new ConcurrentSkipListSet<URL>();
        URLs_to_crawl = new ConcurrentLinkedQueue<URL>();
        URLs_crawled = new ConcurrentSkipListSet<URL>();

        fileInterface = new FileInterface(this);
    }

    public void startCrawl() {
        // Dispose of the interface now that we're done with it..
        if (fileInterface != null) fileInterface.dispose();

        // Parse the CSV file.
//        csvParser = new CSV_Parser();
//        csvParser.parseFile(fileInterface.getFileChosen());
        csvParser = new CSV_Parser("http://www.thesketchfellows.com/",1);

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
