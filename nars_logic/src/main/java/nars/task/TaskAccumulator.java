package nars.task;

import java.util.Collection;
import java.util.TreeSet;

/** priority queue which merges equal tasks and accumulates their budget */
public class TaskAccumulator {

    public final TreeSet<Task> buffer;

    public TaskAccumulator(TaskComparator.Merging mode) {
        this(new TaskComparator(mode));
    }

    public TaskAccumulator(TaskComparator comp) {
        this.buffer = new TreeSet(comp);
        ////this.newTasks = new ConcurrentSkipListSet(new TaskComparator(memory.param.getDerivationDuplicationMode()));
    }

    public void clear() {
        buffer.clear();
    }

    public boolean add(Task t) {
        return buffer.add(t);
    }

    public int size() {
        return buffer.size();
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    public Task removeHighest() {
        return buffer.pollLast();
    }

    public void addAll(Collection<Task> x) {
        buffer.addAll(x);
    }
}
