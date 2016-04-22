import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Daniel on 4/20/2016.
 */
public class NoiseReducer {
    protected String inputPath;
    protected String outputPath;

    NR_FileInterface fileInterface;

    NoiseReducer() {
        // Setup

        fileInterface = new NR_FileInterface(this);
    }

    public void noiseReduce() {

        System.out.println("Your in path was: " + inputPath);
        System.out.println("Your out path was: " + outputPath);

        // Iterate over all of the files in the directory.
        File inDir = new File(inputPath);
        File[] inDirectoryListing = inDir.listFiles();
        if (inDirectoryListing != null) {
            for (File containedFile : inDirectoryListing) {
                // Skip report.html
                if (containedFile.getName().equals("report.html"))
                    continue;

                // Get the file extension.
                String extension = FilenameUtils.getExtension(containedFile.getName());

                if (!extension.equals("html"))
                    continue;

                List<Integer> bitStream;

                try {
                    bitStream = parseFile(containedFile);
                }
                catch (IOException e) {
                    System.err.println("NoiseReducer.noiseReduce: IOException thrown by call to parseFile.");
                }


            }
        }

        System.exit(0);
    }

    // Calls the default one.
    public void noiseReduce(String inPath, String outPath) {
        inputPath = inPath;
        outputPath = outPath;
        noiseReduce();
    }

    /**
     * Called to produce a bitstream from the target file representing tags.
     * @param targetFile - The file to be parsed.
     * @return A List of
     * @throws FileNotFoundException
     */
    private List<Integer> parseFile(File targetFile) throws IOException {
        List<Integer> bitStream = new ArrayList<Integer>();

        // Create scanner
        Document doc = Jsoup.parse(targetFile, "UTF-8");


        return bitStream;
    }

    public static void main(String[] args){
        new NoiseReducer();
    }
}
