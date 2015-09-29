package nars.concept;

import javolution.util.function.Equality;
import nars.task.Task;
import nars.util.data.list.FasterList;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** implements a Task table suitable for Questions and Quests using an ArrayList.
 *  we use an ArrayList and not an ArrayDeque (which is seemingly ideal for the
 *  FIFO behavior) because we can iterate entries by numeric index avoiding
 *  allocation of an Iterator.
 *
 *
 */
public class ArrayListTaskTable extends FasterList<Task> implements TaskTable, Externalizable {

    protected int cap;

    public ArrayListTaskTable() {
        super();
    }

    public ArrayListTaskTable(int cap) {
        super(cap);
        setCapacity(cap);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(cap);
        out.writeInt(size());
        for (int i = 0; i < size(); i++) {
            out.writeObject(get(i));
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setCapacity(in.readInt());
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            this.add((Task)in.readObject());
        }
    }

    @Override
    public void setCapacity(int newCapacity) {
        this.cap = newCapacity;
    }


    /**
     * iterator-less implementation
     */
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
    public Task add(Task t, Equality<Task> equality) {

        if (getFirstEquivalent(t, equality) != null) {
            return t;
        }

        //Memory m = c.getMemory();
        final int siz = size();
        if (siz + 1 > cap) {
            // FIFO, remove oldest question (last)
            /*Task removed = */
            remove(siz - 1);
            //m.emit(Events.ConceptQuestionRemove.class, c, removed /*, t*/);
        }

        add(0, t);

        //m.emit(Events.ConceptQuestionAdd.class, c, t);

        return t;
    }


}

