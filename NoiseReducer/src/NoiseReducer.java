import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 4/20/2016.
 */
public class NoiseReducer {
    protected String inputPath;
    protected String outputPath;
    private ThreadSafeInt processCount;
    protected int numThreads;
    protected File[] inDirectoryListing;
    private List<NoiseReducerRunner> runners;
    private boolean wantTags = true;            // Set this for whether you want tags or not.

    NR_FileInterface fileInterface;

    NoiseReducer() {
        // Setup
        numThreads = 250;
        processCount = new ThreadSafeInt(0);
        fileInterface = new NR_FileInterface(this);
        runners = new ArrayList<NoiseReducerRunner>();
    }

    public void noiseReduce() {
        fileInterface.dispose();

        // Iterate over all of the files in the directory.
        File inDir = new File(inputPath);
        inDirectoryListing = inDir.listFiles();
        if (inDirectoryListing != null) {
            for (int i = 0; i < numThreads && i < inDirectoryListing.length; i++) {
                NoiseReducerRunner runner = new NoiseReducerRunner(i);
                runners.add(runner);
                runner.thread.start();
            }
        }

        for (NoiseReducerRunner runner : runners) {
            try {
                runner.thread.join();
            } catch (InterruptedException e) {
                System.err.println("InterruptedException thrown when trying to join thread "+runner.id);
                e.printStackTrace();
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

    class NoiseReducerRunner implements Runnable {
        int id;
        protected Thread thread;

        NoiseReducerRunner(int id) {
            this.id = id;
            thread = new Thread(this, "Noise Reducer Thread "+id);
        }

        @Override
        public void run() {

            while (true) {
                int fileIndex = processCount.getValAndIncrement();
                if (fileIndex >= inDirectoryListing.length)
                    break;

                System.out.println("Starting to process file "+fileIndex);

                File file = inDirectoryListing[fileIndex];

                // Skip report.html
                if (file.getName().equals("report.html"))
                    continue;

                // Get the file extension.
                String extension = FilenameUtils.getExtension(file.getName());

                if (!extension.equals("html"))
                    continue;

                List<LexTuple> lexTuples;

                try {
                    lexTuples = Lexer.lexFile(file);

                    if (lexTuples != null) {
                        boolean firstLine = true;
                        BufferedWriter outputWriter;

                        int low = 0;
                        int high = lexTuples.size()-1;
                        String separator = "";

                        // Run analysis on lexTupleList.
//                        ContentFinder contentFinder = new ContentFinder(lexTuples);
//                        low = contentFinder.lowPosition;
//                        high = contentFinder.highPosition;
//                        separator = "\n";

                        outputWriter = new BufferedWriter(new FileWriter(outputPath + file.getName()));
                        for (int i = low; i <= high; i++){
                            LexTuple lexTuple = lexTuples.get(i);
                            // Write either everything if you want tags, or just non-tags if you don't.
                            if (wantTags || lexTuple.getBit() != 1) {
                                if (firstLine)
                                    firstLine = false;
                                else if (!separator.equals(""))
                                    outputWriter.write(separator);

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

                System.out.println("Finished processing file "+fileIndex);
            }

            System.out.println("Noise Reducer Thread "+id+" done running.");
            return;
        }
    }
}
