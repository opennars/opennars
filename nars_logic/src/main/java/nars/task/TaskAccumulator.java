package nars.task;

import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Consumer;

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

    /** if size() capacity, remove lowest elements until size() is at capacity
     * @return how many removed
     * */
    public int limit(int capacity, Consumer<Task> onRemoved) {

        int numToRemove = size() - capacity;
        int removed = 0;
        while (numToRemove-- > 0) {
            onRemoved.accept( buffer.pollFirst() );
            removed++;
        }

        return removed;
    }
}
