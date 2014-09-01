package nars.core.state;

import java.io.Serializable;

/** emotional value; self-felt internal mental states; variables used to record emotional values */
public class EmotionState implements Serializable {

    /** average desire-value */
    private float happy;
    /** average priority */
    private float busy;

    public EmotionState() {
    }

    public EmotionState(float happy, float busy) {
        set(happy, busy);
    }

    public void set(float happy, float busy) {
        this.happy = happy;
        this.busy = busy;
    }

    public float happy() {
        return happy;
    }

    public float busy() {
        return busy;
    }

    public void adjustHappy(float newValue, float weight) {
        //        float oldV = happyValue;
        happy += newValue * weight;
        happy /= 1.0f + weight;
        //        if (Math.abs(oldV - happyValue) > 0.1) {
        //            Record.append("HAPPY: " + (int) (oldV*10.0) + " to " + (int) (happyValue*10.0) + "\n");
    }

    public void adjustBusy(float newValue, float weight) {
        //        float oldV = busyValue;
        busy += newValue * weight;
        busy /= (1.0f + weight);
        //        if (Math.abs(oldV - busyValue) > 0.1) {
        //            Record.append("BUSY: " + (int) (oldV*10.0) + " to " + (int) (busyValue*10.0) + "\n");
    }
}
