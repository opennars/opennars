package nars.io;

import nars.io.in.Input;
import nars.util.data.buffer.Source;
import nars.task.Task;

import java.util.function.Supplier;

/** input port for external, ex: sensory events */
public class TaskSource implements Source<Task> {

    private final float attention;
    private final Supplier<Task> input;

    public TaskSource(Supplier<Task> input, float attention) {

        this.attention = attention;
        this.input = input;
    }

    @Override
    public float getAttention() {
        return attention;
    }

    @Override
    public void stop() {
        if (input instanceof Input)
            ((Input)input).stop();
    }



    public Task get() {
        return input.get();
    }

}
