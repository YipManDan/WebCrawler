import org.apache.commons.io.FilenameUtils;

import java.io.*;

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
                if (containedFile.getName().equals("Report.html"))
                {
                    System.out.println("Found it!");
                    continue;
                }

                String extension = FilenameUtils.getExtension(containedFile.getName());
                if (extension != "html") continue;

                System.out.println(containedFile.getName() + " - " + extension);
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

    public static void main(String[] args){
        new NoiseReducer();
    }
}
