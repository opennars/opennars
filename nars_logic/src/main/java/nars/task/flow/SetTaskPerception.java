package nars.task.flow;

import com.gs.collections.api.block.procedure.Procedure2;
import nars.Memory;
import nars.budget.Budget;
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
    final Procedure2<Budget, Budget> merge;

    public SetTaskPerception(Memory m, Consumer<Task> receiver) {
        this(m, receiver, Budget.plus);
    }

    public SetTaskPerception(Memory m, Consumer<Task> receiver, Procedure2<Budget, Budget> merge) {
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
            merge.value(t.getBudget(), existing.getBudget());
        }
    }

    @Override
    public void nextFrame(Consumer<Task> receiver) {
        table.forEach((k, v)->receiver.accept(v));
        table.clear();
    }

    @Override
    public void clear() {
        table.clear();
    }
}
