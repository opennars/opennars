package nars.event;

import nars.Memory;
import nars.NAR;
import nars.util.event.On;

import java.util.function.Consumer;

/** default cycle reaction, called at end of cycle */
abstract public class CycleReaction implements Consumer<Memory> {

    private final On cycleReg;

    public CycleReaction(NAR nar) {
        this(nar.memory);
    }

    public CycleReaction(Memory memory) {
        super();
        cycleReg = memory.eventCycleEnd.on(this);

    }

    public void off() { cycleReg.off(); }

    abstract public void onCycle();

    @Override
    public void accept(Memory memory) {
        onCycle();
    }
}
