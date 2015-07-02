package nars.clock;

import nars.Memory;

/**
 * Created by me on 7/2/15.
 */
public interface Clock {

    /** called when memory reset */
    public void reset();

    /** returns the current time, as measured in units determined by this clock */
    public long time();

    /** called at the beginning of a new cycle */
    public void preCycle();

    default public void preFrame(Memory m) { }

    long timeSinceLastCycle();
}
