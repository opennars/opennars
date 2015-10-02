package nars.util.data.list;

import com.gs.collections.impl.list.mutable.FastList;

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

//    public final Object[] array() {
//        return items;
//    }


    public final void forEach(final Consumer c) {
//        for (Object o : items) {
//            if (o == null) break;
//            c.accept(o);
//        }

        int size = size();
        for (int i = 0; i < size; i++) {
            c.accept(get(i));
        }
    }
}
