

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A class to parse the CSV file the crawler takes as input.
 *
 * @author  Daniel Yeh and Jesse Harder
 * @version 1.0
 * @since   2016-04-06
 */
public class CSV_Parser {
    public URL seedURL;
    public List<URL> seedURLs;
    public int numberOfPagesToCrawl;
    public URL URLRestriction;
    public List<URL> URLRestrictions;

    // Default constructor does nothing.
    CSV_Parser() {}

    CSV_Parser(URL seedURL, int numPages) {
        this.seedURL = seedURL;
        this.numberOfPagesToCrawl = numPages;
        this.URLRestriction = null;
    }

    CSV_Parser(String seedURL, int numPages) {
        this.numberOfPagesToCrawl = numPages;
        this.URLRestriction = null;
        try{
            this.seedURL = new URL(seedURL);
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException in CSV_Parser2 constructor.");
            e.printStackTrace();
        }
    }

    CSV_Parser(URL seedURL, int numPages, URL URLRestriction) {
        this.seedURL = seedURL;
        this.numberOfPagesToCrawl = numPages;
        this.URLRestriction = URLRestriction;
    }

    CSV_Parser(String seedURL, int numPages, String URLRestriction) {
        this.numberOfPagesToCrawl = numPages;
        try{
            this.seedURL = new URL(seedURL);
            this.URLRestriction = new URL(URLRestriction);
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException in CSV_Parser2 constructor.");
            e.printStackTrace();
        }
    }

    public void parseFile(File inputFile) {
        seedURLs = new ArrayList<URL>();
        URLRestrictions = new ArrayList<URL>();
        try {
            Scanner csvScanner = new Scanner(inputFile);
            csvScanner.useDelimiter(",");

            // Read in the seedURL
            if (csvScanner.hasNext()) {
                String urlString = csvScanner.next();
                // Add protocol if not given.
                if (!urlString.startsWith("http")) {
                    urlString = "http://"+urlString;
                }
                seedURL = new URL(urlString);
                System.out.println("seedURL: " + seedURL.toString());
                seedURLs.add(seedURL);
            }
            else
                System.err.println("CSV File Reading Error: Could not read seedURL");

            // Read in the number of pages to crawl.
            if (csvScanner.hasNextInt()) {
                numberOfPagesToCrawl = csvScanner.nextInt();
                System.out.println("numberOfPagesToCrawl" + numberOfPagesToCrawl);
            }
            else
                System.err.println("CSV File Reading Error: Could not read number of pages to crawl");

            // Read in the seedURL restriction field, if one is there.
            if (csvScanner.hasNext()) {
                try {
                    String urlString = csvScanner.next();
                    // Add protocol if not given.
                    if (!urlString.startsWith("http")) {
                        urlString = seedURL.getProtocol() + "://" + urlString;
                    }
                    URLRestriction = new URL(urlString);
                    URLRestrictions.add(URLRestriction);
                }
                catch (MalformedURLException e) {
                    System.out.println("No URLRestriction");
                    URLRestriction = null;
                }
            }
            else
                URLRestriction = null;
        } catch(FileNotFoundException exception)
        {
            System.out.println("FileNotFoundException in CSV_Parser2.parseFile(): The file " + inputFile.getPath() + " was not found.");
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException in CSV_Parser2.parseFile().");
            e.printStackTrace();
        }

    }
}
