package nars.util.bag;


/**
* Created by me on 1/20/15.
*/
public class DDNodePool<E> extends DequePool<DDNodePool.DD<E>> {

    /** node in a linked list; wraps each Item stored in the bag */
    public static class DD<E> {

        public E item;

        /** can be used to identify the current manager of this node, if multiple lists are involved */
        protected int owner;

        public DD<E> next;
        public DD<E> prev;

        @Override
        public String toString() {
            return item + "[" + prev.item + "." + owner + "," + next.item + "]";
        }

        public int owner() {
            return owner;
        }
    }

    public DDNodePool(int preallocate) {
        super(preallocate);
    }

    public DD<E> create() {
        return new DD<E>();
    }

    @Override
    public void put(DD<E> i) {
        i.owner = -1;
        i.next = i.prev = null;
        i.item = null;
        super.put(i);
    }

    public DD<E> get(E item, int owner) {
        DD<E> x = get();
        x.item = item;
        x.owner = owner;
        return x;
    }
}
