package nars.concept;

import javolution.util.function.Equality;
import nars.Events;
import nars.Memory;
import nars.task.Task;

import java.util.ArrayList;

/** implements a Task table suitable for Questions and Quests using an ArrayList.
 *  we use an ArrayList and not an ArrayDeque (which is seemingly ideal for the
 *  FIFO behavior) because we can iterate entries by numeric index avoiding
 *  allocation of an Iterator.
 */
public class ArrayListTaskTable extends ArrayList<Task> implements TaskTable {

    protected int cap;

    public ArrayListTaskTable() {
        super();
    }
    public ArrayListTaskTable(int cap) {
        super(cap);
        setCapacity(cap);
    }

    @Override
    public void setCapacity(int newCapacity) {
        this.cap = newCapacity;
    }


    /** iterator-less implementation */
    @Override
    public Task getFirstEquivalent(final Task t, final Equality<Task> e) {
        final int n = size();
        for (int i = 0; i < n; i++) {
            Task a = get(i);
            if (e.areEqual(a, t))
                return a;
        }
        return null;
    }


    @Override
    public Task add(Task t, Equality<Task> equality, Concept c) {

        if (getFirstEquivalent(t, equality)!=null) {
            return t;
        }

        Memory m = c.getMemory();
        if (size() + 1 > cap) {
            // FIFO, remove oldest question
            Task removed = remove(0);
            m.emit(Events.ConceptQuestionRemove.class, c, removed /*, t*/);
        }

        add(t);

        m.emit(Events.ConceptQuestionAdd.class, c, t);

        return t;
    }

}
