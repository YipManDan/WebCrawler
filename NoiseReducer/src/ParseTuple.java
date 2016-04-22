/**
 * Created by Daniel on 4/21/2016.
 */
public class ParseTuple {
    private int position;
    private int bit;

    ParseTuple(int position, int bit){
        this.position = position;
        this.bit = bit;
    }

    public int getBit() {
        return bit;
    }

    public int getPosition() {
        return position;
    }

}
