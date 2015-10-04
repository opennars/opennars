package nars.util.event;

import nars.util.data.list.FasterList;

import java.util.function.IntFunction;

/**
 * Thread safe list which produces arrays for fast iteration
 * these arrays are like copy-on-write-array-list except
 * are reusable and null-terminated. so if the size shrinks,
 * it does not need to reallocate or pad the array with nulls.
 *
 * use C[] nullTerminatedArray() to access this array, don't
 * change it without a good reason (it will be shared), and
 * iterate it in sequence and stop at the first null (this is the
 * end).
 */
public class ArraySharingList<C>  {

    protected final FasterList<C> data = new FasterList();
    private final IntFunction<C[]> arrayBuilder;
    private C[] array = null;

    public ArraySharingList(IntFunction<C[]> arrayBuilder) {
        super();
        this.arrayBuilder = arrayBuilder;
    }

    public boolean add(C x) {
        if (data.add(x)) {
            updateArray();
            return true;
        }
        return false;
    }
    public boolean remove(C x) {
        if (data.remove(x)) {
            updateArray();
            return true;
        }
        return false;
    }

    public final int size() {
        return data.size();
    }

    /** may be null; ignore its size, it will be at least 1 element larger than the size of the list */
    public C[] nullTerminatedArray() {
        return this.array;
    }

    private void updateArray() {

        //TODO for safe atomicity while the events are populated, buffer additions to a sub-list,
        //and apply them if a flag is set on the next read

        final FasterList<C> consumers = this.data;

        if (!consumers.isEmpty()) {
            C[] a = this.array;
            if (a == null)
                a = arrayBuilder.apply(data.size()+1);  //+1 for padding
            this.array = consumers.toNullTerminatedUnpaddedArray(a);
        }
        else {
            this.array = null;
        }

    }


    public boolean isEmpty() {
        return size()!=0;
    }

}
