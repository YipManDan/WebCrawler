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
        int highNext;
        /*
        integer i will point to the upper inclusive bound of low.
        integer j will point to the lower inclusive bound of high.
        (mid) will be between i and j, exclusive.
         */

        low = 0;
        sum = 0;
        bestSum = sum;
        //Initialize highNext which will be the initial values for high
        highNext = 0;
        for(j = 1; j < parseTuples.size(); j++){
            highNext += parseTuples.get(j).getBit();
        }

        /*
        O([n^2-n]/2) = O(n^2)
        Loops checks all possible combinations of low, high, and mid to determine the maximum.
         */
        for(i = 0; i < parseTuples.size()-1; i++){
            low += parseTuples.get(i).getBit();
            mid = 0;
            high = highNext;
            highNext -= parseTuples.get(i + 1).getBit();

            //Prior to loop we can visualize i and j as next to each other
            //When the loop begins, j move to the right once and mid has a width of one.
            for(j = i + 2; j < parseTuples.size(); j++){
                mid += (1 - parseTuples.get(j - 1).getBit());
                high -= parseTuples.get(j - 1).getBit();
                sum = low + mid + high;
                if(sum > bestSum){
                    bestSum = sum;
                    lowPosition = parseTuples.get(i).getPosition();
                    highPosition = parseTuples.get(j).getPosition();
                }

            }
        }



    }

    /**
     * Uh... not sure if I even want this... Maybe closer to even 1/3 split is better? I dunno...
     * @param i index of new lowPosition
     * @param j index of new highPosition
     * @return  true if better(?) false if not
     */
    private boolean isCloserToMid(int i, int j){
        int lowNew, highNew;
        int lowOld, highOld;
        int midPosition = (int)(parseTuples.size()/2 + .5);
        lowNew = Math.abs(midPosition - parseTuples.get(i).getPosition());
        highNew = Math.abs(midPosition - parseTuples.get(j).getPosition());
        lowOld = Math.abs(midPosition - lowPosition);
        highOld = Math.abs(midPosition - highPosition);
        return true;
    }

}
