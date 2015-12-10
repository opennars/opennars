package nars.time;

import nars.Memory;
import nars.util.event.On;

/** increments time on each frame */
public class CycleClock implements Clock {


    long t;
    private Memory memory;
    private On handler = null;

    @Override
    public synchronized void clear(Memory m) {

        if (this.memory!=null && this.memory!=m) {
            handler.off();
            handler = null;
        }

        t = 0;

        this.memory = m;

        if (m!=null) {
            handler = m.eventCycleEnd.on(mm -> {
                t++;
            });
        }
    }

    @Override
    public final long time() {
        return t;
    }


    @Override
    public final void preFrame() {
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
