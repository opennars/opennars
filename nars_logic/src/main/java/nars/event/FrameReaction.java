package nars.event;

import nars.Events;
import nars.NAR;

/**  call at the end of a frame (a batch of cycles) */
abstract public class FrameReaction extends NARReaction {

    public FrameReaction(NAR nar) {
        super(nar, Events.FrameEnd.class);
    }

    @Override public void event(Class event, Object[] args) {
        if (event == Events.FrameEnd.class) {
            onFrame();
        }
    }

    abstract public void onFrame();
}