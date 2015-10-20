package nars.link;

import nars.Memory;
import nars.bag.tx.BagActivator;
import nars.task.Task;

/** adjusts budget of items in a Bag. ex: merge */
public class TaskLinkBuilder extends BagActivator<Task,TaskLink> {

    public final Memory memory;
    private float forgetCycles;
    private long now;

    public TaskLinkBuilder(Memory m) {
        super();
        this.memory = m;
    }

    public void setTask(Task t) {

        setKey(t);
        setBudget(t.getBudget());

        final Memory m = this.memory;

        this.forgetCycles = m.duration() *
                m.taskLinkForgetDurations.floatValue();

        this.now = m.time();
    }

    @Override
    public final long time() {
        return now;
    }

    @Override
    public final float getForgetCycles() {
        return forgetCycles;
    }

    @Override
    public final TaskLink newItem() {
         return new TaskLink(name());
    }

    @Override
    public String toString() {
        return name().toString();
    }
}
