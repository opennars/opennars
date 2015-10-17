package nars.io;

import nars.Memory;
import nars.task.Task;
import nars.util.event.Active;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by me on 10/16/15.
 */
public abstract class TaskPerception extends Active implements Consumer<Task> {
    /**
     * max # of inputs to perceive per cycle; -1 means unlimited (attempts to drains input to empty each cycle)
     */
    public final AtomicInteger inputsMaxPerCycle = new AtomicInteger(1);
    /**
     * determines if content can enter
     */
    protected final Predicate<Task> filter;
    /**
     * where to send output
     */
    protected final Consumer<Task> receiver;

    public TaskPerception(Memory m, Predicate<Task> filter, Consumer<Task> receiver) {
        super();
        add(
            m.eventInput.on(this),
            m.eventDerived.on(this),
            m.eventFrameStart.on((M) -> send()),
            m.eventReset.on((M) -> clear() )
        );


        this.filter = filter;
        this.receiver = receiver;
    }

    @Override
    public abstract void accept(Task t);

    public abstract void send();

    public abstract void clear();
}
