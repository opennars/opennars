package nars.util.event;

import nars.Memory;
import nars.NAR;

import java.util.function.Consumer;

/**  call at the end of a frame (a batch of cycles) */
public abstract class FrameReaction implements Consumer<NAR> {

    private On reg;

    protected FrameReaction(NAR nar) {
        this(nar.memory);
    }
    protected FrameReaction(Memory m) {
        reg = m.eventFrameStart.on(this);
    }

    public void off() {
        if (reg!=null) {
            reg.off();
            reg = null;
        }
    }

    @Override
    public void accept(NAR nar) {
        onFrame();
    }


    @Deprecated
    public abstract void onFrame();
}