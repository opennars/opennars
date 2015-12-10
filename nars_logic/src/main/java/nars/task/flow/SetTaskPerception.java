package nars.task.flow;

import nars.Global;
import nars.Memory;
import nars.task.Task;

import java.util.Set;
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

    final Set<Task> task = Global.newHashSet(1);

    public SetTaskPerception(Memory m, Consumer<Task> receiver) {
        super(m, receiver);
    }

    @Override
    public void forEach(Consumer<? super Task> each) {
        task.forEach(each);
    }

    @Override
    public void accept(Task t) {
        task.add(t);
    }

    @Override
    public void nextFrame(Consumer<Task> receiver) {
        task.forEach(receiver);
        task.clear();
    }

    @Override
    public void clear() {
        task.clear();
    }
}
