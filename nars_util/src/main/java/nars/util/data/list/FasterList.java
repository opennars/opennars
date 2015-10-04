package nars.util.data.list;

import com.gs.collections.impl.list.mutable.FastList;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Less-safe faster FastList with direct array access
 *
 * TODO override the array creation to create an array
 * of the actual type necessary, so that .array()
 * can provide the right array when casted
 */
public class FasterList<X> extends FastList<X> {

    public FasterList() {
        super();
    }

    public FasterList(int capacity) {
        super(capacity);
    }

    public FasterList(Collection<X> x) {
        super(x);
    }

    @Override
    final public X get(final int index) {
        //if (index < this.size) {
        return this.items[index];
        //}
    }

    /** use with caution.
     *    --this could become invalidated so use it as a snapshot
     *    --dont modify it
     *    --when iterating, expect to encounter a null
     *      at any time, and if this happens, break your loop
     *      early
     **
     */
    public final Object[] array() {
        return items;
    }

    @Override
    public <E> E[] toArray(E[] array) {
        array = toArrayUnpadded(array);
        if (array.length > this.size)
        {
            array[this.size] = null;
        }
        return array;
    }

    /** does not pad the remaining values in the array with nulls */
    public <E> E[] toArrayUnpadded(E[] array) {
        if (array.length < this.size)
        {
            array = (E[]) Array.newInstance(array.getClass().getComponentType(), this.size);
        }
        System.arraycopy(this.items, 0, array, 0, this.size);
        return array;
    }


    public <E> E[] toNullTerminatedUnpaddedArray(E[] array) {
        final int s = this.size; //actual size
        if (array.length < (s+1)) {
            array = (E[]) Array.newInstance(array.getClass().getComponentType(), s+1);
        }
        System.arraycopy(this.items, 0, array, 0, s);
        array[s] = null;
        return array;
    }

    public final void forEach(final Consumer c) {
//        for (Object o : items) {
//            if (o == null) break;
//            c.accept(o);
//        }

        int size = size();
        final Object[] a = array();
        for (int i = 0; i < size; i++) {
            final Object j = a[i];
            if (j == null) break; //end of list
            c.accept(j);
        }
    }


}
