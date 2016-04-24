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

    NR_FileInterface fileInterface;

    NoiseReducer() {
        // Setup

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

                List<LexTuple> lexTupleList = null;

                try {
                    lexTupleList = Lexer.lexFile(containedFile);

                    // Run analysis on lexTupleList.
//                    ContentFinder contentFinder = new ContentFinder(lexTupleList);
                }
                catch (IOException e) {
                    lexTupleList = null;
                    System.err.println("NoiseReducer.noiseReduce: IOException thrown by call to lexFile when opening file to analyze.");
                    System.exit(1);
                }

                // Write out the tokens in the list in the identified reason.
                if (lexTupleList != null) {
                    BufferedWriter outputWriter;
                    try {
                        outputWriter = new BufferedWriter(new FileWriter(outputPath + containedFile.getName()));
                        for (int i = 0; i < lexTupleList.size(); i++){
                            outputWriter.write(lexTupleList.get(i).getToken() + "\n");
                        }
                        outputWriter.close();
                    }
                    catch (IOException e) {
                        System.err.println("NoiseReducer.noiseReduce: IOException thrown by call to lexFile when opening file to write out to..");
                        e.printStackTrace();
                    }
                }
                // The lexTupleList was null, meaning that there isn't anything to write out.
                else {
                    System.err.println("NoiseReducer.noiseReduce: Lex Tuple list was null.");
                    System.exit(1);
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

    public static void main(String[] args){
        new NoiseReducer();
    }
}
