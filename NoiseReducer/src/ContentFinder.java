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
        int midNext, highNext;
        /*
        integer i will point to the upper inclusive bound of low.
        integer j will point to the lower inclusive bound of high.
        (mid) will be between i and j, exclusive.
         */

        low = 0;
        sum = 0;
        bestSum = sum;
        //Initialize midNext and highNext which will be the initial values for mid and high
        midNext = (1 - parseTuples.get(1).getBit());
        highNext = 0;
        for(j = 2; j < parseTuples.size(); j++){
            highNext += parseTuples.get(j).getBit();
        }

        for(i = 0; i < parseTuples.size()-1; i++){
            low += parseTuples.get(i).getBit();
            mid = midNext;
            high = highNext;
            midNext += (1 - parseTuples.get(i + 1).getBit());
            highNext -= parseTuples.get(i + 2).getBit();

            //initially low starts at 0 then.
            //initially high starts where? I think at i + 1?
            for(j = i + 2; j < parseTuples.size(); j++){
                mid += (1 - parseTuples.get(j - 1).getBit());
                high -= parseTuples.get(j).getBit();
                sum = low + mid + high;
                if(sum > bestSum){
                    bestSum = sum;
                    lowPosition = parseTuples.get(i).getPosition();
                    highPosition = parseTuples.get(j).getPosition();
                }

            }
        }



    }

}
