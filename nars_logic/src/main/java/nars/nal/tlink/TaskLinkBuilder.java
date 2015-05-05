package nars.nal.tlink;

import nars.Memory;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.bag.tx.BagActivator;

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
//        if (template == null)
//            setKey(TaskLink.key(TermLink.SELF, null, t));
//        else
//            setKey(TaskLink.key(template.type, template.index, t));
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
        if (template == null)
            return new TaskLink(getTask(), getBudgetRef());
        else
            return new TaskLink(getTask(), template, getBudgetRef());
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
