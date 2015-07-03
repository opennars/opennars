package nars.concept;

import javolution.util.function.Equality;
import nars.task.Task;

/** holds a set of ranked question/quests tasks */
public interface TaskTable extends Iterable<Task> {

    public void setCapacity(int newCapacity);

    /** number of items in this collection */
    public int size();

    public void clear();

    public boolean isEmpty();

    /** attempt to insert a task.
     *
     * @param c the concept in which this occurrs
     * @return:
     *      the input value, 'q' if it it was added to the table
     *      a previous stored task if this was a duplicate
     *
     */
    public Task add(Task q, Equality<Task> e, Concept c);

    /**
     *
     * @return null if no duplicate was discovered, or the first Task that matched if one was
     */
    default public Task getFirstEquivalent(final Task t, final Equality<Task> e) {
        for (final Task a : this) {
            if (e.areEqual(a, t))
                return a;
        }
        return null;
    }
}
