package nars.io;

import nars.nal.entity.Task;
import reactor.function.Supplier;

/** input port for external, ex: sensory events */
public class TaskSource extends Source<Task> {

    private final Supplier<Task> input;

    public TaskSource(Supplier<Task> input, float priority) {
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
