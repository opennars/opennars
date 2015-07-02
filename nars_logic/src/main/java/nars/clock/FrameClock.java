package nars.clock;

import nars.Memory;

/** increments time on each frame */
public class FrameClock extends CycleClock {

    @Override
    public void preFrame(Memory m) {
        tick();
    }

    @Override
    public void preCycle() {
        //nothing
    }
}
