/**
 * Created by JHarder on 4/14/16.
 */
class ThreadSafeInt {
    private int counter;

    /* Constructors */
    ThreadSafeInt() {counter = 0;}
    ThreadSafeInt(int startValue) {counter = startValue;}

    /* Accesor */
    public int val() {
        synchronized (this) {
            return counter;
        }
    }

    /* Mutators */

    public void increment() {increment(1);}

    public void increment(int amount) {
        synchronized (this) {
            counter += amount;
        }
    }

    public void decrement() {decrement(1);}

    public void decrement(int amount) {
        synchronized (this) {
            counter -= amount;
        }
    }

    public void setCounter(int amount) {
        synchronized (this) {
            counter = amount;
        }
    }
}
