package nars.util.data;

import com.gs.collections.impl.list.mutable.FastList;

/**
 * Unsafe faster FastList with direct array access
 */
public class FasterList<X> extends FastList<X> {

    public FasterList() {
        super();
    }

    public FasterList(int capacity) {
        super(capacity);
    }

    @Override
    final public X get(final int index) {
        //if (index < this.size) {
        return this.items[index];
        //}
    }

    public X[] array() {
        return items;
    }

}
