import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * Created by Daniel on 4/22/2016.
 */
public class RobotsChecker {
    //given a url, class will download robots.txt
    //Will parse robots.txt and add to dont access list? or return true/false, can access?
    //Maybe it will be in between spider and adding to queue?

    private URL url;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private String line, userAgent;
    private String[] parsedLine;
    List<URL> disallowedURLs;

    RobotsChecker(URL url){
        this.url = url;
        System.out.println("Robots URL check:" + url.getHost());


//        try{
//
//            inputStream = (new URL(url.getHost() + "robots.txt")).openStream();
//            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//        }
//        catch (Exception e){
//            System.err.println("Exception at Robots.txt: " + e);
//        }
    }

//    private void getDisallowList(){
//        try {
//            while ((line = bufferedReader.readLine()) != null) {
//                System.out.println(line);
//                if (line.contains("User-agent:")) {
//                    parsedLine = line.split(" ");
//                    userAgent = parsedLine[1];
//                    System.out.println("User-agent is: " + userAgent);
//                } else if (line.length() == 0)
//                    continue;
//                else if (userAgent.equals("*")) {
//                    parsedLine = line.split(" ");
//                    disallowedURLs.add(new URL(url.getHost() + parsedLine[1]));
//                }
//
//            }
//        }
//        catch (Exception e){
//            System.err.println("Exception at getDisallowList: " + e);
//        }
//
//        System.out.printf("Disallowed List:");
//        for (URL url: disallowedURLs) {
//            System.out.println(url);
//        }
//        System.out.println("Done with Disallowed List");
//        System.out.println("");
//    }
}
