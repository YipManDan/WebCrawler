import java.util.List;

/**
 * Created by Daniel on 4/21/2016.
 */
public class ContentFinder {
    private List<ParseTuple> parseTuples;
    protected int lowPosition;
    protected int highPosition;

    ContentFinder(List<ParseTuple> parseTuples){
        this.parseTuples = parseTuples;

        lowPosition = 0;
        highPosition = parseTuples.get(parseTuples.size()-1).getPosition();
        findContent();
    }

    void findContent(){
        //indexes
        int i, j;
        //sums
        int low, mid, high;
        int sum, bestSum;
        int midNext = 0, highNext = 0;

        //Get initial values for low, mid, and high
        low = parseTuples.get(lowPosition).getBit();
        mid = parseTuples.get(lowPosition + 1).getBit();
        high = 0;
        for(i = lowPosition + 2; i < parseTuples.size(); i++){
            high += parseTuples.get(i).getBit();
        }

        for(i = lowPosition; i < parseTuples.size()-1; i++){
            j = i + 1;
            midNext = midNext + (1 - parseTuples.get(i + 1).getBit());
            highNext = highNext - parseTuples.get(j).getBit();
        }



    }

}
