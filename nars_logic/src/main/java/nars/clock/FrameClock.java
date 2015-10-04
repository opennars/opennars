package nars.clock;

import nars.Memory;

/** increments time on each frame */
public class FrameClock implements Clock {


    long t;

    @Override
    public void clear() {
        t = 0;
    }

    @Override
    public final long time() {
        return t;
    }


    @Override
    public final void preFrame(Memory m) {
        t++;
    }

    @Override
    public long elapsed() {
        return 1;
    }



    @Override
    public String toString() {
        return Long.toString(t);
    }

}
