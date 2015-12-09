package nars.task;

import nars.Memory;
import nars.util.event.Active;

/**
 * Task which is specifically for collecting statistics about
 * its budget dynamics across time and reacting to
 * lifecycle events which are empty stubs in its
 * super-classes
 *
 */
public abstract class MeterTask extends MutableTask {

    private final Active active = new Active();

    @Override
    protected void onNormalized(Memory memory) {
        active.add(
                memory.eventFrameStart.on((n) -> onFrame(memory))
        );
    }

    abstract void onFrame(Memory memory);

}
