import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 4/22/2016.
 */
public class RobotsChecker {

    private URL url;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private String line, userAgent;
    private String[] parsedLine;

    protected List<URL> disallowedURLs;
    protected int crawlDelay;

    RobotsChecker(URL url){
        try {
            this.url = new URL(url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        crawlDelay = -1;
        disallowedURLs = new ArrayList<URL>();
        String robotURL = "http://" + url.getHost() + "/robots.txt";

        try{
            System.out.println("Accessing: " + robotURL);
            inputStream = (new URL(robotURL)).openStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }
        catch (Exception e){
            System.out.println("Exception at Robots.txt: " + e);
            return;
        }

        getDisallowList();
    }

    protected void getDisallowList(){
        String disallowedURL;
        userAgent = "";
        try {
            while ((line = bufferedReader.readLine()) != null) {
//                System.out.println(line);
                if (line.length() == 0)
                    userAgent = "";
                else if (line.contains("User-agent:")) {
                    parsedLine = line.split(" ");
                    userAgent = parsedLine[1];
//                    System.out.println("User-agent is: " + userAgent);
                }
                else if (userAgent.equals("*")) {
                    parsedLine = line.split(" ");
                    String lineStart = parsedLine[0];
                    if (lineStart.toLowerCase().startsWith("crawl-delay"))
                        crawlDelay = Integer.parseInt(parsedLine[1]);
                    else if (parsedLine[0].toLowerCase().startsWith("disallow")) {
                        disallowedURL = prepareURL(parsedLine[1]);
//                        System.out.println("Disallowed: " + disallowedURL);
                            disallowedURLs.add(new URL("http://" + url.getHost() + disallowedURL));
                    }
                }
            }

            bufferedReader.close();
        }
        catch (Exception e){
            System.err.println("Exception at getDisallowList: ");
            e.printStackTrace();
        }

//        System.out.printf("Disallowed List:");
//        for (URL url: disallowedURLs) {
//            System.out.println(url);
//        }
        System.out.println("Done with Disallowed List");
        System.out.println("");
    }

    public boolean isAllowed(URL url){
        for(URL checkURL : disallowedURLs){
            if(url.toString().matches(checkURL.toString()))
                return false;
        }
        return true;
    }

    private String prepareURL(String disallowedURL) {
        disallowedURL = disallowedURL.replace(".", "\\.");
        disallowedURL = disallowedURL.replace("*",".*");
        return disallowedURL;
    }
}
