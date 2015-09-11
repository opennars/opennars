package nars.event;

import nars.Memory;
import nars.NAR;
import nars.util.event.DefaultTopic;

import java.util.function.Consumer;

/** default cycle reaction, called at end of cycle */
abstract public class CycleReaction implements Consumer<Memory> {

    private final DefaultTopic.Subscription reg;

    public CycleReaction(NAR nar) {
        this(nar.mem());
    }

    public CycleReaction(Memory memory) {
        super();
        reg = memory.eventCycleEnd.on(this);
    }

    abstract public void onCycle();

    @Override
    public void accept(Memory memory) {
        onCycle();
    }
}
