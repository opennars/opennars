package nars.nal.nal7;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by me on 9/27/15.
 */
public class AtomicDuration extends AtomicInteger {

    /**
     * this represents the amount of time (ex: cycles) in proportion to a duration in which
     * Interval resolution calculates.  originally, NARS had a hardcoded duration of 5
     * and an equivalent Interval scaling factor of ~1/2 (since ln(E) ~= 1/2 * 5).
     * Since duration is now adjustable, this factor approximates the same result
     * with regard to a "normalized" interval scale determined by duration.
     */
    double linear = 0.5;

    transient double log; //caches the value here
    transient int lastValue = -1;

    public AtomicDuration() {
        super();
    }


    public AtomicDuration(int v) {
        super(v);
    }

    public void setLinear(double linear) {
        this.linear = linear;
        update();
    }

    protected void update() {
        int val = get();
        lastValue = val;
        this.log = Math.log(val * linear);
    }

    public double getSubDurationLog() {
        int val = get();
        if (lastValue != val) {
            update();
        }
        return log;
    }

}
