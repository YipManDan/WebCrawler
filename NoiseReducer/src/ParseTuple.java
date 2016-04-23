/**
 * Created by Daniel on 4/21/2016.
 */
public class ParseTuple {
    private String token;
    private int position;
    private int bit;

    ParseTuple(String token, int position, int bit){
        this.token = token;
        this.position = position;
        this.bit = bit;
    }

    public String getToken() {
        return token;
    }

    public int getBit() {
        return bit;
    }

    public int getPosition() {
        return position;
    }

}
