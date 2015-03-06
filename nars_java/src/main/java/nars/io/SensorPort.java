package nars.io;

import nars.logic.entity.Task;
import reactor.function.Supplier;

/** input port for external, sensory events */
public class SensorPort extends InPort<Task> {

    private final Supplier<Task> input;

    public SensorPort(Supplier<Task> input, float priority) {
        super(priority);
        this.input = input;
    }


    @Override
    public void stop() {
        if (input instanceof Input)
            ((Input)input).stop();
    }

    @Override
    public Task get() {
        return input.get();
    }

}
