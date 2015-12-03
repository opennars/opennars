package nars.util.data.list;

import com.gs.collections.impl.list.mutable.primitive.IntArrayList;

/**
 * Created by me on 12/3/15.
 */
public class FasterIntArrayList extends IntArrayList {

    public FasterIntArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public final int[] array() {
        return items;
    }


    @Override public final int get(int index) {
        return this.items[index];
    }

    /** quickly remove the final elements without nulling them by setting the size pointer */
    final public void popTo(int index) {
        /*if (newSize < 0)
            throw new RuntimeException("negative index");*/
        this.size = index+1;
    }

}
