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
 * @since   2016-05-11
 */
public class CSV_Parser2 extends CSV_Parser {
    // Default constructor does nothing.
    CSV_Parser2() {
        seedURL = null;
        URLRestriction = null;
        seedURLs = new ArrayList<URL>();
        URLRestrictions = new ArrayList<URL>();
    }

    public void parseFile(File inputFile) {
        try {
            Scanner csvScanner = new Scanner(inputFile);
            String line;

            // Read in the number of pages to crawl.
            if (csvScanner.hasNextLine()) {
                line = csvScanner.nextLine();
                numberOfPagesToCrawl = Integer.parseInt(line);
                System.out.println("numberOfPagesToCrawl" + numberOfPagesToCrawl);
            }
            else
                System.err.println("CSV File Reading Error: Could not read number of pages to crawl");

            // Read in the seedURLs
            if (csvScanner.hasNext()) {
                line = csvScanner.nextLine();
                String[] seedURLStrings = line.split(",");
                int numSeedURLs = 0;
                for (String urlString : seedURLStrings) {
                    // Add protocol if not given.
                    if (!urlString.startsWith("http")) {
                        urlString = "http://"+urlString;
                    }
                    seedURLs.add(new URL(urlString));
                    System.out.println("seedURL " + seedURLs.get(numSeedURLs).toString());
                    numSeedURLs++;
                }
            }
            else
                System.err.println("CSV File Reading Error: Could not read seedURL");

            // Read in the seedURL restriction field, if one is there.
            if (csvScanner.hasNext()) {
                try {
                    String[] urlStrings = csvScanner.next().split(",");
                    // Add protocol if not given.
                    for(String urlString : urlStrings) {
                        URLRestrictions.add(new URL(urlString));
                    }
                }
                catch (MalformedURLException e) {
                    System.out.println("No URLRestriction");
                }
            }
        } catch(FileNotFoundException exception)
        {
            System.out.println("FileNotFoundException in CSV_Parser2.parseFile(): The file " + inputFile.getPath() + " was not found.");
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException in CSV_Parser2.parseFile().");
            e.printStackTrace();
        }

    }
}
