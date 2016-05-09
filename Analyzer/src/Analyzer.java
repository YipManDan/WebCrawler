import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JHarder on 4/24/16.
 */
public class Analyzer {
    protected Map<String, BigInteger> allWords;
    protected String path;

    Analyzer() {
        allWords = new ConcurrentHashMap<String, BigInteger>();
    }

    public void analyze(String path) {
        this.path = path;

        List<SubAnalyzer> subAnalyzerList = new ArrayList<SubAnalyzer>();

        File inDir = new File(path);
        File[] inDirectoryListing = inDir.listFiles();

        // Create sub-threads to analyze all of the files.
        if (inDirectoryListing != null) {
            for (File containedFile : inDirectoryListing) {

                // Check the file extension.
                String extension = FilenameUtils.getExtension(containedFile.getName());
                if (!extension.equals("html"))
                    continue;

                SubAnalyzer subAnalyzer = new SubAnalyzer(containedFile);
                subAnalyzerList.add(subAnalyzer);
                subAnalyzer.thread.start();
            }
        }

        // Wait for all sub-threads to finish.
        for (SubAnalyzer subAnalyzer : subAnalyzerList) {
            try {
                subAnalyzer.thread.join();
            } catch (InterruptedException e) {
                System.err.println("Analyzer.analyze encountered InterruptedException error when joining thread for file " +subAnalyzer.file.getName());
                e.printStackTrace();
            }
        }

        // Print all of the values and counts in the hash map.
//        synchronized (allWords) {
//            for (Map.Entry<String,BigInteger> entry : allWords.entrySet()) {
//                System.out.println(entry.getKey() + " - " + entry.getValue());
//            }
//        }

        // Write the csv file.

        synchronized (allWords) {
            BufferedWriter outputWriter;
            boolean firstLine = true;
            try {
                outputWriter = new BufferedWriter(new FileWriter(path + "results.csv"));
                for (Map.Entry<String, BigInteger> entry : allWords.entrySet()) {
                    if (firstLine)
                        firstLine = false;
                    else
                        outputWriter.write("\n");

                    outputWriter.write(entry.getKey() + "," + entry.getValue());
                }
                outputWriter.close();
            } catch (IOException e) {
                System.err.println("IOException when opening report.csv to output results.");
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        Analyzer analyzer = new Analyzer();
        analyzer.analyze("/Users/JHarder/Desktop/WebCrawlerStuff/repositories for analysis/nr_twitter_NPH/");
    }

    /* Start code for sub analyzer. */

    class SubAnalyzer implements Runnable{
        protected File file;
        protected Map<String, BigInteger> words;
        protected Thread thread;

        SubAnalyzer(File file) {
            this.file = file;
            words = new HashMap<String, BigInteger>();
            thread = new Thread(this, "Sub Analyzer for "+file.getName());
        }

        @Override
        public void run() {
//            System.out.println("Starting to analyze "+file.getName());
            try {
                Scanner scanner = new Scanner(file);
                String token;

                // Read in all of the tokens in the file.
                while (scanner.hasNextLine()) {
                    token = scanner.nextLine();
                    BigInteger count;

                    if (words.containsKey(token))
                    {
                        count = words.get(token);
                        count = count.add(BigInteger.ONE);
                    }
                    else
                        count = BigInteger.ONE;

                    words.put(token, count);
                }

                scanner.close();

                // Add all of your counts into the total count hash map.
                synchronized (allWords) {
                    for (Map.Entry<String, BigInteger> entry : words.entrySet()) {
                        String key = entry.getKey();
                        BigInteger value = entry.getValue();
                        BigInteger count;

                        if (allWords.containsKey(key))
                        {
                            count = allWords.get(key);
                            count = count.add(value);
                        }
                        else
                            count = value;

                        allWords.put(key, count);
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("SubAnalyzer.run for file "+file.getName()+" was unable to open the file.");
                e.printStackTrace();
            }

//            System.out.println("Done analyzing "+file.getName());
        }
    }
}
