package nars.link;

import nars.Memory;
import nars.bag.tx.BagActivator;
import nars.budget.Budget;
import nars.task.Sentence;
import nars.task.Task;

/** adjusts budget of items in a Bag. ex: merge */
public class TaskLinkBuilder extends BagActivator<Sentence,TaskLink> {

    TermLinkTemplate template;
    private Task task;
    public final Memory memory;
    private float forgetCycles;
    private long now;


    public TaskLinkBuilder(Memory memory) {
        super();
        this.memory = memory;
    }

    public void setTask(Task t) {
        this.task = t;
//        if (template == null)
//            setKey(TaskLink.key(TermLink.SELF, null, t));
//        else
//            setKey(TaskLink.key(template.type, template.index, t));
        setKey(t.sentence);
        setBudget(t);
        this.forgetCycles = memory.param.cycles(
                memory.param.taskLinkForgetDurations.floatValue()
        );
        this.now = memory.time();
    }

    @Override
    public long time() {
        return now;
    }

    @Override
    public float getRelativeThreshold() {
        return 0;
    }

    @Override
    public float getForgetCycles() {
        return forgetCycles;
    }

    public Task getTask() {
        return task;
    }

    public void setTemplate(TermLinkTemplate template) {
        this.template = template;
    }

    @Override
    public TaskLink newItem() {
        if (template == null)
            return new TaskLink(getTask(), getBudget());
        else
            return new TaskLink(getTask(), template, getBudget());
    }


    @Override
    public String toString() {
        if (template==null)
            return task.toString();
        else
            return template + " " + task;
    }
}
