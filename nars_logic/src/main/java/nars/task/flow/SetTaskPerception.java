package nars.task.flow;

import nars.Memory;
import nars.budget.BudgetMerge;
import nars.task.Task;
import nars.util.data.map.UnifriedMap;

import java.util.function.Consumer;

/**
 * Buffer for tasks which is duplicate-free but unsorted.
 * It only supports inputting all its contents at once
 * to fairly apply what it contains.  This makes it faster
 * than SortedTaskPerception since the input Tasks should
 * theoretically have the same ultimate outcome even if
 * they are inserted in different orders.
 */
public final class SetTaskPerception extends TaskPerception {

    final UnifriedMap<Task,Task> table = new UnifriedMap<>();
    final BudgetMerge merge;


    public SetTaskPerception(Memory m, Consumer<Task> receiver, BudgetMerge merge) {
        super(m, receiver);
        this.merge = merge;
    }

    @Override
    public void forEach(Consumer<? super Task> each) {
        table.forEach(each);
    }

    @Override
    public void accept(Task t) {
        Task existing = table.put(t, t);
        if (existing!=null) {
            merge.merge(t.getBudget(), existing.getBudget(), 1f);
        }
    }

    @Override
    public void nextFrame(Consumer<Task> receiver) {
        //table.forEach((k, v)->receiver.accept(v));
        table.forEachValue(receiver::accept);
        table.clear();
    }

    @Override
    public void clear() {
        table.clear();
    }
}
