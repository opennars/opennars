package nars.logic.entity.tlink;

import nars.core.Memory;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import nars.logic.entity.TaskLink;
import nars.util.bag.select.BagActivator;

/** adjusts budget of items in a Bag. ex: merge */
public class TaskLinkBuilder extends BagActivator<Sentence,TaskLink> {

    TermLinkTemplate template;
    private Task task;
    public final Memory memory;


    public TaskLinkBuilder(Memory memory) {
        super();
        this.memory = memory;
    }

    public void setTask(Task t) {
        this.task = t;
        setKey(t.sentence);
    }

    public Task getTask() {
        return task;
    }

    public void setTemplate(TermLinkTemplate template) {
        this.template = template;
    }

    @Override
    public TaskLink newItem() {
        int recordLength = memory.param.termLinkRecordLength.get();
        if (template == null)
            return new TaskLink(getTask(), budget, recordLength);
        else
            return new TaskLink(getTask(), template, budget, recordLength);
    }


    @Override
    public TaskLink updateItem(TaskLink taskLink) {
        return null;
    }

    @Override
    public String toString() {
        if (template==null)
            return task.toString();
        else
            return template + " " + task;
    }
}
