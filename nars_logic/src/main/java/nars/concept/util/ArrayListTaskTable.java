package nars.concept.util;

import com.google.common.collect.Iterators;
import com.gs.collections.api.block.procedure.Procedure2;
import javolution.util.function.Equality;
import nars.Memory;
import nars.budget.Budget;
import nars.task.Task;
import nars.term.TermMetadata;
import nars.truth.Truth;
import nars.util.event.ArraySharingList;

import java.util.Arrays;

/**
 * implements a Task table suitable for Questions and Quests using an ArrayList.
 * we use an ArrayList and not an ArrayDeque (which is seemingly ideal for the
 * FIFO behavior) because we can iterate entries by numeric index avoiding
 * allocation of an Iterator.
 */
public class ArrayListTaskTable extends ArraySharingList<Task> implements TaskTable {

    protected int capacity = 0;


    /**
     * warning this will create a 0-capacity table,
     * rejecting all attempts at inputs.  either use the
     * other constructor or change capacity after construction.
     */
    public ArrayListTaskTable() {
        this(0);
    }

    public ArrayListTaskTable(int capacity) {
        super(i -> new Task[i]);
        setCapacity(capacity);
    }

//    @Override
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//        setCapacity(in.readInt());
//        int n = in.readInt();
//        for (int i = 0; i < n; i++) {
//            this.add((Task)in.readObject());
//        }
//    }
//
//
//    @Override
//    public void writeExternal(ObjectOutput out) throws IOException {
//        out.writeInt(capacity);
//        int s = size();
//        out.writeInt(s);
//
//        if (s == 0) return;
//
//        Task[] a = getCachedNullTerminatedArray();
//        for (int i = 0; i < s; i++) {
//            out.writeObject(a[i]);
//        }
//    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TaskTable)) return false;
        TaskTable t = (TaskTable) obj;
        return getCapacity() == t.getCapacity() &&
                Iterators.elementsEqual(iterator(), t.iterator());
    }

    @Override
    public void setCapacity(int newCapacity) {
        this.capacity = newCapacity;
        data.ensureCapacity(newCapacity);
    }


    /**
     * iterator-less implementation
     */
    @Override
    public Task getFirstEquivalent(final Task t, final Equality<Task> e) {
        if (isEmpty()) return null;

        final Task[] aa = getCachedNullTerminatedArray();
        Task a;
        for (int i = 0; null != (a = aa[i++]); ) {
            if (e.areEqual(a, t))
                return a;
        }
        return null;
    }


    @Override
    public Task add(Task t, Equality<Task> equality, Procedure2<Budget, Budget> duplicateMerge, Memory m) {

        Task existing = getFirstEquivalent(t, equality);
        if (existing != null) {

            if (existing != t) {
                duplicateMerge.value(existing.getBudget(), t.getBudget());
                m.remove(t, "PreExisting TaskTable Duplicate");
            }

            return existing;
        }

        //Memory m = c.getMemory();
        final int siz = size();
        if (siz + 1 > capacity) {
            // FIFO, remove oldest question (last)
            /*Task removed = */remove(siz - 1);

            //m.remove(removed, "TaskTable FIFO Out");

            //m.emit(Events.ConceptQuestionRemove.class, c, removed /*, t*/);
        }

        add(0, t);

        //m.emit(Events.ConceptQuestionAdd.class, c, t);

        return t;
    }


    public final boolean contains(Task t) {
        //        //equality:
//        //  1. term (given because it is looking up in concept)
//        //  2. truth
//        //  3. occurrence time
//        //  4. evidential set

        if (isEmpty()) return false;

        if (TermMetadata.has(t.getTerm()))
            return false; //special equality condition

        Truth taskTruth = t.getTruth();
        long taskOccurrrence = t.getOccurrenceTime();
        long[] taskEvidence = t.getEvidence();

        final Task[] aa = getCachedNullTerminatedArray();
        for (Task x : aa) {

            if (x == null) return false;

            if (

                //different truth value
                (x.getTruth().equals(taskTruth)) &&

                //differnt occurence time
                (x.getOccurrenceTime() == taskOccurrrence) &&

                //differnt evidence
                (Arrays.equals(x.getEvidence(), taskEvidence))
            )
                return true;
        }

        return false;

    }

}

