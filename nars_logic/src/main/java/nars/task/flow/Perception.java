package nars.task.flow;

import nars.task.Task;
import nars.util.data.buffer.Source;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Input percept buffer
 */
public interface Perception extends Consumer<Source<Task>>,Supplier<Task> {
    @Override
    void accept(Source<Task> input);


    @Override
    Task get();

    default Task pop() {
        return get();
    }

    default Task pop(float minPriority) {
        Task t;
        while ((t = get())!=null) {
            if (t.getPriority() >= minPriority)
                return t;
        }
        return null;
    }


    void clear();

    boolean isEmpty();
}
