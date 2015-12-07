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
abstract public class MeterTask extends MutableTask {

    private final Active active = new Active();

    @Override
    public Task normalize(Memory memory) {
        Task t = super.normalize(memory);
        if (t!=null) {

            active.add(
                    memory.eventFrameStart.on((n) -> {
                        onFrame(memory);
                    })
            );
        }

        return t;
    }

    abstract void onFrame(Memory memory);

}
