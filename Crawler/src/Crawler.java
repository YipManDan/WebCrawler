import java.net.URL;
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
    // Data
    private ThreadSafeInt numPagesCrawled;
    private Set<URL> recentlyAccessedURLs;  // For the URLs that should not be crawled again yet.
    private Queue<URL> URLs_to_crawl;       // For the URLs scraped and queued but not yet crawled
    private Set<URL> URLs_crawled;          // For the URLs already crawled.

    // Contained Classes
    private FileInterface fileInterface;
    private CSV_Parser csvParser;

    Crawler () {
        numPagesCrawled = new ThreadSafeInt(0);
        recentlyAccessedURLs = new ConcurrentSkipListSet<>();
        URLs_to_crawl = new ConcurrentLinkedQueue<>();
        URLs_crawled = new ConcurrentSkipListSet<>();

        fileInterface = new FileInterface();
        // I don't know how you make the program wait here until the file interface is done getting user input.

        csvParser = new CSV_Parser();
        csvParser.parseFile(fileInterface.getFileChosen());

        // Create Spiders and make them run.

        // Do stuff with what the spiders gathered.

    }
}
