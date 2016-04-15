/**
 * Created by JHarder on 4/14/16.
 */
public class Crawler {
    private FileInterface fileInterface;
    private CSV_Parser csvParser;

    Crawler () {
        csvParser = new CSV_Parser();
        fileInterface = new FileInterface();
    }
}
