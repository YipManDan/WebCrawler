import java.io.File;
import java.io.FileNotFoundException;
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

        // Analyze all of the files.
        if (inDirectoryListing != null) {
            for (File containedFile : inDirectoryListing) {
                SubAnalyzer subAnalyzer = new SubAnalyzer(containedFile);
                subAnalyzerList.add(subAnalyzer);
                subAnalyzer.thread.start();
            }
        }

        for (SubAnalyzer subAnalyzer : subAnalyzerList) {
            try {
                subAnalyzer.thread.join();
            } catch (InterruptedException e) {
                System.err.println("Analyzer.analyze encountered InterruptedException error when joining thread for file " +subAnalyzer.file.getName());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Analyzer analyzer = new Analyzer();
        analyzer.analyze("/Users/JHarder/Desktop/noise_reduced/");
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
            try {
                Scanner scanner = new Scanner(file);
                String token;

                while (scanner.hasNext()) {
                    token = scanner.next();
                    System.out.println(token);
                }
            } catch (FileNotFoundException e) {
                System.out.println("SubAnalyzer.run for file "+file.getName()+" was unable to open the file.");
                e.printStackTrace();
            }
        }
    }
}
