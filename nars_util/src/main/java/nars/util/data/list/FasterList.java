package nars.util.data.list;

import com.gs.collections.impl.list.mutable.FastList;

import java.util.Collection;

/**
 * Less-safe faster FastList with direct array access
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

    public X[] array() {
        return items;
    }

}
