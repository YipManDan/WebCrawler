import org.apache.commons.io.FilenameUtils;

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
    private int processCount;

    NR_FileInterface fileInterface;

    NoiseReducer() {
        // Setup
        processCount = 0;
        fileInterface = new NR_FileInterface(this);
    }

    public void noiseReduce() {
        fileInterface.dispose();
//        System.out.println("Input path: "+inputPath);
//        System.out.println("Output path: "+outputPath);

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

                List<LexTuple> lexTuples;

                try {
                    lexTuples = Lexer.lexFile(containedFile);

                    // Run analysis on lexTupleList.
                    ContentFinder contentFinder = new ContentFinder(lexTuples);

                    System.out.println("Content Finder finished.");
                    System.out.println("Lower position: "+contentFinder.lowPosition);
                    System.out.println("High position: "+contentFinder.highPosition);

                    // Write out the tokens in the list in the identified reason.
                    if (lexTuples != null) {
                        boolean firstLine = true;
                        BufferedWriter outputWriter;

                        outputWriter = new BufferedWriter(new FileWriter(outputPath + containedFile.getName()));
                        for (int i = contentFinder.lowPosition; i <= contentFinder.highPosition; i++){
                            LexTuple lexTuple = lexTuples.get(i);
                            if (lexTuple.getBit() != 1) {
                                if (firstLine)
                                    firstLine = false;
                                else
                                    outputWriter.write("\n");

                                outputWriter.write(lexTuple.getToken().toLowerCase());
                            }
                        }
                        outputWriter.close();
                    }
                    // The lexTupleList was null, meaning that there isn't anything to write out.
                    else {
                        System.err.println("NoiseReducer.noiseReduce: Lex Tuple list was null.");
                        System.exit(1);
                    }
                }
                catch (IOException e) {
                    lexTuples = null;
                    System.err.println("NoiseReducer.noiseReduce: IOException thrown by call to lexFile.");
                    System.err.println(e.getMessage());
                    System.exit(1);
                }

                System.out.println("Finished processing "+processCount+" files.");
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

    protected void endProgram(){
        System.out.println("Closing Sequence");
        System.exit(1);
    }

    public static void main(String[] args){
        new NoiseReducer();
    }
}
