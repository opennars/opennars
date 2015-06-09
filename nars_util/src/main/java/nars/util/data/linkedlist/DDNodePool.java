package nars.util.data.linkedlist;


import nars.util.data.DequePool;

/**
* Created by me on 1/20/15.
*/
public class DDNodePool<E> extends DequePool<DD<E>> {

    public DDNodePool(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public DD<E> create() {
        return new DD();
    }

    @Override
    public void put(final DD<E> i) {
        i.owner = -1;
        i.next = i.prev = null;
        i.item = null;
        super.put(i);
    }

    public DD<E> get(final E item, final int owner) {
        final DD<E> x = get();
        x.item = item;
        x.owner = owner;
        return x;
    }



}
