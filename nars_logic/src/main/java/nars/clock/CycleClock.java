package nars.clock;


import nars.Memory;

/**
 * internal, subjective time (1 cycle = 1 time step)
 */
public class CycleClock implements Clock {

    long t;

    @Override
    public void clear() {
        t = 0;
    }

    @Override
    public long time() {
        return t;
    }

    public void tick() {
        t++;
    }

    @Override
    public void preFrame(Memory m) {

    }

    @Override
    public void preCycle() {
        tick();
    }

    @Override
    public long timeSinceLastCycle() {
        return 1;
    }

    @Override
    public String toString() {
        return Long.toString(t);
    }


}
