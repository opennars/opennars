package nars.task;

import nars.Memory;
import nars.util.event.Active;

/**
 * Task which is specifically for collecting statistics about
 * its budget dynamics across time.
 *
 */
abstract public class MeterTask extends FluentTask {

    private final Active active = new Active();

    public MeterTask() {
        super();
    }

    @Override
    public boolean init(Memory memory) {
        active.add(
            memory.eventFrameStart.on((n) -> {
                onFrame(memory);
            })
        );

        return false;
    }

    abstract void onFrame(Memory memory);

}
