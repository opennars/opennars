package nars.util.event;

import nars.util.data.list.FasterList;

import java.util.concurrent.atomic.AtomicBoolean;
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
    private AtomicBoolean change = new AtomicBoolean(false);

    public ArraySharingList(IntFunction<C[]> arrayBuilder) {
        super();
        this.arrayBuilder = arrayBuilder;
    }

    public final boolean add(C x) {
        if (data.add(x)) {
            change.set(true);
            return true;
        }
        return false;
    }

    public final boolean remove(C x) {
        if (data.remove(x)) {
            change.set(true);
            return true;
        }
        return false;
    }

    public final int size() {
        return data.size();
    }

    /** may be null; ignore its size, it will be at least 1 element larger than the size of the list */
    public C[] nullTerminatedArray() {
        if (change.compareAndSet(true,false))
            updateArray();
        return this.array;
    }

    private final C[] updateArray() {

        //TODO for safe atomicity while the events are populated, buffer additions to a sub-list,
        //and apply them if a flag is set on the next read

        final FasterList<C> consumers = this.data;

        C[] a;
        if (!consumers.isEmpty()) {
            a = this.array;
            if (a == null)
                a = arrayBuilder.apply(data.size()+1);  //+1 for padding
            a = consumers.toNullTerminatedUnpaddedArray(a);
        }
        else {
            a = null;
        }

        return this.array = a;
    }


    public boolean isEmpty() {
        return size()!=0;
    }

}
