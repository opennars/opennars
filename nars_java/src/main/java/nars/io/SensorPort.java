package nars.io;

import nars.logic.entity.Task;
import reactor.function.Supplier;

/** input port for external, sensory events */
public class SensorPort extends InPort<Task> {

    private final Supplier<Task> input;
    private long creationTimeOverride = -1;

    public SensorPort(Supplier<Task> input, float priority) {
        super(priority);
        this.input = input;
    }

    public void setCreationTimeOverride(long creationTime) {
        this.creationTimeOverride = creationTime;
    }

    @Override
    public void stop() {
        if (input instanceof Input)
            ((Input)input).stop();
    }

    @Override
    public Task get() {
        Task t = input.get();
        if (t!=null) {
            if (creationTimeOverride != -1) {
                t.sentence.stamp.setCreationTime(creationTimeOverride);
            }
        }
        return t;
    }

}
