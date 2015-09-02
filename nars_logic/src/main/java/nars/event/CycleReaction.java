package nars.event;

import nars.Memory;
import nars.NAR;
import nars.util.event.Observed;

import java.util.function.Consumer;

/** default cycle reaction, called at end of cycle */
abstract public class CycleReaction implements Consumer<Memory> {

    private final Observed.DefaultObserved.DefaultObservableRegistration reg;

    public CycleReaction(NAR nar) {
        super();
        reg = nar.memory.eventCycleEnd.on(this);
    }

    abstract public void onCycle();

    @Override
    public void accept(Memory memory) {
        onCycle();
    }
}
