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

                List<LexTuple> lexTupleList;

                try {
                    lexTupleList = parseFile(containedFile);
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
    private List<LexTuple> parseFile(File targetFile) throws IOException {
        List<LexTuple> lexTuples = new ArrayList<LexTuple>();

        Scanner scanner = new Scanner(targetFile);
        String token = "";

        FileInputStream inputStream = new FileInputStream(targetFile);
        char currentChar = 's', prevChar;
        boolean readingTag = false;
        int counter = 0;

        while (inputStream.available() > 0) {
            prevChar = currentChar;
            currentChar = (char) inputStream.read();

            if (Character.isWhitespace(currentChar)) {
                if (!readingTag && token != "") {
//                    System.out.println(token);
                    lexTuples.add(new LexTuple(token,counter,0));
                    token = "";
                }
            }
            else {
                // Start a tag
                if (currentChar == '<' && prevChar != '\\') {
                    if (token != "") {
//                        System.out.println(token);
                        lexTuples.add(new LexTuple(token,counter,0));
                        token = "";
                    }
                    readingTag = true;
                }
                // End a tag.
                else if (readingTag && currentChar == '>' && prevChar !='\\') {
                    readingTag = false;
                    token += currentChar;
//                    System.out.println(token);
                    lexTuples.add(new LexTuple(token,counter,1));
                    token = "";
                    continue;
                }
//                System.out.println(""+currentChar+" - "+token+" - "+readingTag);
                token += currentChar;
            }

        }

        for (LexTuple tuple : lexTuples) {
            System.out.println(tuple.getToken() + " - " + tuple.getBit());
        }

        return lexTuples;
    }

    public static void main(String[] args){
        new NoiseReducer();
    }
}
