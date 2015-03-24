package nars.event;

import nars.Events;
import nars.NAR;

/** default cycle reaction, called at end of cycle */
abstract public class CycleReaction extends AbstractReaction {

    public CycleReaction(NAR nar) {
        super(nar, Events.CycleEnd.class);
    }

    @Override public void event(Class event, Object[] args) {
        if (event == Events.CycleEnd.class) {
            onCycle();
        }
    }

    abstract public void onCycle();
}
