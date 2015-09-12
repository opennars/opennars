package nars.event;

import nars.NAR;
import nars.util.event.DefaultTopic;

import java.util.function.Consumer;

/**  call at the end of a frame (a batch of cycles) */
abstract public class FrameReaction implements Consumer<NAR> {

    private DefaultTopic.Subscription reg;

    public FrameReaction(NAR nar) {
        reg = nar.memory.eventFrameEnd.on(this);
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


    @Deprecated abstract public void onFrame();
}