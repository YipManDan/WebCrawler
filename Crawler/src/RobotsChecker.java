import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle robots.txt file from webhosts.
 * The class will download the robots.txt file in order to determine
 *  which pages a spider shall be allowed on as well as the crawl-delay.
 *
 * @Author Daniel Yeh and Jesse Harder
 */
public class RobotsChecker {

    //Data
    private URL url;
    private String line, userAgent;
    private String[] parsedLine;

    // Data which the parent class to can use to determine allowed status and crawl-delay
    protected List<URL> disallowedURLs;
    protected int crawlDelay = -1;

    // For downloading the robots.txt file
    private InputStream inputStream;
    private BufferedReader bufferedReader;

    /**
     * Constructor of RobotsChecker
     * @param url   The url the crawler wants to crawl
     */
    RobotsChecker(URL url){

        // Store a local copy of the url
        try {
            this.url = new URL(url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        disallowedURLs = new ArrayList<URL>();

        // Generate the URL of the robots.txt file
        System.out.println(url.getProtocol());
        String robotURL = url.getProtocol()+"://" + url.getHost() + "/robots.txt";

        // Create a bufferedReader to download the robots.txt file
        try{
            System.out.println("Accessing: " + robotURL);
            inputStream = (new URL(robotURL)).openStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }
        catch (Exception e){
            System.err.println("Exception at Robots.txt: " + e);
            return;
        }

        //Call method to download and store relevant information
        getDisallowList();
    }

    /**
     * Method will download the robots.txt file line-by-line and skip
     * to the relevant sections directed towards our crawler.
     * Disallowed links will be stored and if a crawl-delay is specified it will be saved.
     */
    protected void getDisallowList(){

        String disallowedURL;
        userAgent = "";

        try {
            // Loop until the end of the robots.txt file
            while ((line = bufferedReader.readLine()) != null) {

                //If the line is empty, then the file is switching sections. Reset the userAgent variable
                if (line.length() == 0)
                    userAgent = "";

                else if (line.contains("User-agent:")) {
                    // Parse and store the specified user-agent
                    parsedLine = line.split(" ");
                    userAgent = parsedLine[1];
                }

                // Check if the user-agent specified in this block of text is specific to our crawler
                else if (userAgent.equals("*")) {
                    // Parses the line
                    parsedLine = line.split(" ");
                    String lineStart = parsedLine[0];

                    // If the line indicates the crawl-delay, store it
                    if (lineStart.toLowerCase().startsWith("crawl-delay"))
                        crawlDelay = Integer.parseInt(parsedLine[1]);

                    // If the line lists a URL path to disallow, then add to disallow list
                    else if (parsedLine[0].toLowerCase().startsWith("disallow")) {
                        disallowedURL = prepareURL(parsedLine[1]);
                        disallowedURLs.add(new URL("http://" + url.getHost() + disallowedURL));

                    }
                }
            }

            //Close the bufferedReader and inputStream
            bufferedReader.close();
            inputStream.close();
        }
        catch (Exception e){
            System.err.println("Exception at getDisallowList: ");
            e.printStackTrace();
        }
    }

    /**
     * Public method to determine if input URL is allowed by the host.
     * @param url   URL the Crawler wants to access
     * @return  Return true if the Crawler is allowed to crawl the page, else false.
     */
    public boolean isAllowed(URL url){

        //Loop through all URL's in the list
        for(URL checkURL : disallowedURLs){

            //Use regex matching to determine if the URL is disallowed
            if(url.toString().matches(checkURL.toString()))
                return false;
        }

        return true;

    }

    /**
     * Method to change URL to allow for regex matching
     * @param disallowedURL URL to be added to disallowList
     * @return  Returns a String which a modified URL allowing for regex matching.
     */
    private String prepareURL(String disallowedURL) {
        disallowedURL = disallowedURL.replace(".", "\\.");
        disallowedURL = disallowedURL.replace("*",".*");
        return disallowedURL;
    }
}
