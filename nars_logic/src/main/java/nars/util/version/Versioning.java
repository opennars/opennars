package nars.util.version;

import nars.util.data.DequePool;
import nars.util.data.list.FasterList;

/** versioning context that holds versioned instances */
public class Versioning extends FasterList<Versioned> {

    private int now = 0;

    /** serial id's assigned to each Versioned */
    private int nextID = 1;


    @Override
    public String toString() {
        return now + ":" + super.toString();
    }

    public final int now() {
        return now;
    }

    /** start a new version with a commit, returns current version  */
    public final int newChange(Versioned v) {
        int c = commit();
        add(v);
        return c;
    }

    /** track change on current commit, returns current version */
    public final int continueChange(Versioned v) {
        add(v);
        return now;
    }

    public final int commit() {
        return ++now;
    }


    /** reverts to previous state */
    public final void revert() {
        revert(now -1);
    }

    /** reverts/undo to previous state */
    public final void revert(int when) {
        int was = now;
        if (was == when)
            return; //no change

        doRevert(when, was);
    }

    public void doRevert(int when, int was) {
        if (was < when)
            throw new RuntimeException("reverting to future time");
        now = when;

        int s = size()-1;
        if (s == -1) return; //empty

        while (get(s).revertNext(when)) {
            if (--s < 0)
                break;
        }

        popTo(s);
    }

    /** assigns a new serial ID to a versioned item for its use as a hashcode */
    public final int track() {
        return nextID++;
    }

    static final int initiALPOOL_CAPACITY = 16;
    static final int stackLimit = 12;

    final DequePool<FasterList> valueStackPool = new FasterListDequePool();
    final DequePool<int[]> intStackPool = new intDequePool();

    public final <X> FasterList<X> newValueStack() {
        //from heap:
        //return new FasterList(16);

        //object pooling value stacks from context:
        return valueStackPool.get();
    }

    public final int[] newIntStack() {
        return intStackPool.get();
    }

    /** should only call this when v will never be used again because its buffers are recycled here */
    public <X> void onDeleted(Versioned v) {
        FasterList vStack = v.value;

        //TODO maybe flush these periodically for GC
        //vStack.clear();

        //TODO reject arrays that have grown beyond a certain size
        valueStackPool.put(vStack);
        intStackPool.put(v.array());
    }

    private static final class FasterListDequePool extends DequePool<FasterList> {
        public FasterListDequePool() {
            super(Versioning.initiALPOOL_CAPACITY);
        }

        @Override public FasterList create() {
            return new FasterList(8);
        }
    }

    private static final class intDequePool extends DequePool<int[]> {
        public intDequePool() {
            super(Versioning.initiALPOOL_CAPACITY);
        }

        @Override public int[] create() {
            return new int[stackLimit];
        }
    }


//    public boolean toStackString() {
//
//
//
//    }

}
