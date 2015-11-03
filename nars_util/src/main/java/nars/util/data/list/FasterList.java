package nars.util.data.list;

import com.gs.collections.impl.list.mutable.FastList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.IntFunction;

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

    /** uses array directly */
    public FasterList(X... x) {
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
    public final X[] array() {
        return items;
    }

//    /** use this to get the fast null-terminated version;
//     *  slightly faster; use with caution
//     * */
//    public <E> E[] toNullTerminatedArray(E[] array) {
//        array = toArrayUnpadded(array);
//        final int size = this.size;
//        if (array.length > size) {
//            array[size] = null;
//        }
//        return array;
//    }

    public X[] toArray(IntFunction<X[]> arrayBuilder) {
//HACK broken return the internal array if of the necessary size, otherwise returns a new array of precise size
//        X[] current = this.array();
//        if (size() == current.length)
//            return current;
        return fillArray(arrayBuilder.apply(size()));
    }


//    /** does not pad the remaining values in the array with nulls */
//    X[] toArrayUnpadded(X[] array) {
//        if (array.length < this.size)        {
//            //resize larger
//            array = (X[]) Array.newInstance(array.getClass().getComponentType(), this.size);
//        }
//        return fillArray(array);
//    }

    public final X[] fillArrayNullPadded(X[] array) {
        int s = this.size;
        int l = array.length;
        if (array == null || array.length < (s+1)) {
            array = (X[]) Array.newInstance(array.getClass().getComponentType(), s+1);
        }
        System.arraycopy(this.items, 0, array, 0, s);
        if (s<l)
            Arrays.fill(array, s, l, null); //pad remainder
        return array;
    }
    public final X[] fillArray(X[] array) {
        int s = this.size;
        int l = array.length;
        System.arraycopy(this.items, 0, array, 0, s);
        if (s<l)
            Arrays.fill(array, s, l, null); //pad remainder
        return array;
    }


//    public final X[] toNullTerminatedUnpaddedArray(X[] array) {
//        final int s = this.size; //actual size
//        if (array.length < (s+1)) {
//            array = (X[]) Array.newInstance(array.getClass().getComponentType(), s+1);
//        }
//        System.arraycopy(this.items, 0, array, 0, s);
//        array[s] = null;
//        return array;
//    }

    public final void forEach(final Consumer c) {
        for (Object j : array()) {
            if (j == null) break; //end of list
            c.accept(j);
        }
    }

}
