package nars.task.flow;

import nars.Memory;
import nars.task.Task;
import nars.util.event.Active;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Receives tasks which are input to a memory
 */
public abstract class TaskPerception implements Consumer<Task> {


    /**
     * determines if content can enter
     */
    protected final Predicate<Task> filter;

    /**
     * where to send output
     */
    protected final Consumer<Task> receiver;

    private final Active active = new Active();

    public TaskPerception(Memory m,
                          Predicate<Task> filter,
                          Consumer<Task> receiver) {
        super();

        this.filter = filter;
        this.receiver = receiver;

        active.add(
            m.eventInput.on(this),
            m.eventFrameStart.on((M) -> send()),
            m.eventReset.on((M) -> clear() )
        );

    }

    @Override
    public abstract void accept(Task t);

    public abstract void send();

    public abstract void clear();
}
