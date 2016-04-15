import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by JHarder on 4/13/16.
 */
public class CSV_Parser {
    private String URL = "";
    private int numberOfPagesToCrawl = -1;
    private String URLRestriction = "";

    public void parseFile(File inputFile) {
        try {
            Scanner csvScanner = new Scanner(inputFile);
            csvScanner.useDelimiter(",");

            // Read in the URL
            if (csvScanner.hasNext())
                URL = csvScanner.next();
            else
                System.err.println("CSV File Reading Error: Could not read URL");

            // Read in the number of pages to crawl.
            if (csvScanner.hasNextInt())
                numberOfPagesToCrawl = csvScanner.nextInt();
            else
                System.err.println("CSV File Reading Error: Could not read number of pages to crawl");

            // Read in the URL restriction field, if one is there.
            if (csvScanner.hasNext())
                URLRestriction = csvScanner.next();
        } catch(FileNotFoundException exception)
        {
            System.out.println("Error in CSV_Parser.parseFile(): The file " + inputFile.getPath() + " was not found.");
        }

    }
}
