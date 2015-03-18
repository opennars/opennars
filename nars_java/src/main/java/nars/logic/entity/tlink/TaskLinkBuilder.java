package nars.logic.entity.tlink;

import nars.core.Memory;
import nars.io.Symbols;
import nars.logic.entity.Task;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.util.bag.select.BagActivator;

/** adjusts budget of items in a Bag. ex: merge */
public class TaskLinkBuilder extends BagActivator<String,TaskLink> {

    TermLinkTemplate template;
    private Task task;
    public final Memory memory;


    public TaskLinkBuilder(Memory memory) {
        super();
        this.memory = memory;
    }

    public void setTask(Task t) {
        this.task = t;
        if (template == null)
            setKey(TermLinkTemplate.prefix(TermLink.SELF, null, false) + Symbols.TLinkSeparator + t.sentence);
        else
            setKey(TermLinkTemplate.prefix(template.type, template.index, false) + Symbols.TLinkSeparator + t.sentence);
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
