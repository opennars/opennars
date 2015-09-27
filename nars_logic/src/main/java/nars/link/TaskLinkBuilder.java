package nars.link;

import nars.Memory;
import nars.bag.tx.BagActivator;
import nars.task.Sentence;
import nars.task.Task;

/** adjusts budget of items in a Bag. ex: merge */
public class TaskLinkBuilder extends BagActivator<Sentence,TaskLink> {

    private Task task;
    public final Memory memory;
    private float forgetCycles;
    private long now;

    public TaskLinkBuilder(Memory m) {
        super();
        this.memory = m;
    }

    public void setTask(Task t) {
        this.task = t;
//        if (template == null)
//            setKey(TaskLink.key(TermLink.SELF, null, t));
//        else
//            setKey(TaskLink.key(template.type, template.index, t));
        setKey(t);
        setBudget(t.getBudget());
        this.forgetCycles = memory.durationToCycles(
                memory.taskLinkForgetDurations.floatValue()
        );
        this.now = memory.time();
    }

    @Override
    public final long time() {
        return now;
    }

    @Override
    public final float getForgetCycles() {
        return forgetCycles;
    }

    public final Task getTask() {
        return task;
    }

    @Override
    public final TaskLink newItem() {
         return new TaskLink(getTask(), getBudget());
    }

    @Override
    public String toString() {
        return task.toString();
    }
}
