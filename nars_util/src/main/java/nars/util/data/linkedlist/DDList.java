package nars.util.data.linkedlist;

import java.util.function.Consumer;

/** from: http://algs4.cs.princeton.edu/13stacks/DoublyLinkedList.java.html */
public class DDList<E> implements Iterable<E> {


    static class Sentinel<E> extends DD<E> {
        public Sentinel(int id) {
            owner = id; }
    }
    static class HeadSentinel<E> extends Sentinel<E> {
        public HeadSentinel(int id) { super(id); }
    }
    static class TailSentinel<E> extends Sentinel<E> {
        public TailSentinel(int id) { super(id); }
    }

    private final DDNodePool<E> pool;

    int size;        // number of elements on list

    int id;
    public final HeadSentinel<E> pre;
    public final TailSentinel<E> post;    // sentinels before first and after last item


    public DDList(int id, DDNodePool<E> nodepool) {
        this.id = id;
        pool = nodepool;
        pre = new HeadSentinel(id);
        post = new TailSentinel(id);

        _clear();
    }

    public void clear() {
        _clear();
        changed();
    }
    
    protected void _clear() {

        if ((size > 0) && (pool.isEnabled())) {
            //TODO items may not need to be detached in this iterative loop if the endpoints can be attached to their conclusions
            DD current = getFirstNode();
            do {
                DD next = current.next;
                pool.put(detach(current));
                current = next;
            } while (size > 0);
        }

        size = 0;
        pre.next = post;
        post.prev = pre;
    }

    public void delete() {
        clear();
//        pre.next = null;
//        post.prev = null;
    }


    /** called when this list is changed in any way */
    protected void changed() {

    }

    public boolean isEmpty() {
        /*
        //THIS CAN OCCURR IN THE MIDDLE OF AN 'UPDATE' TRANSACTION, SO EITHER
        DISABLE THIS TEST DURING TRANSACTION OR REMOVE IT
        if (Parameters.DEBUG) {
            if ((size ==0) && (pre.next!=post) && (post.prev!=pre))
                throw new RuntimeException("Invalid empty state: " + this);
        }
        */
        return size == 0;
    }

    public int size() {
        return size;
    }

    /** add the raw item to the list, will be bagged */
    public DD<E> add(E item) {
        if (item == null) throw new RuntimeException("Bag requires non-null items");
        DD<E> d = pool.get(item, id);
        return add( d );
    }


    public DD<E> getFirstNode() {
        //if (isEmpty()) return null;
        DD<E> x = pre.next;
        if (x instanceof Sentinel) {
            return x.next;
        }
        return x;
    }
    public DD<E> getLastNode() {
        //if (isEmpty()) return null;
        DD<E> x = post.prev;
        if (x instanceof Sentinel) return x.prev;
        return x;
    }

    /** if there are more than 1 element, remove the first element and attach it to the end */
    public void rotate() {
        if (size() < 2) return;
        add(detach(getFirstNode()));
    }

    public E getFirst() {
        //if (isEmpty()) return null;
        return pre.next.item;
    }
    public E getLast() {
        //if (isEmpty()) return null;
        return post.prev.item;
    }

    public DD<E> add(DD<E> x) {

        if (x == null) throw new RuntimeException("attempt to add null element");
        if (x.owner()!=getID()) throw new RuntimeException("add of item already owned by level " + x.owner());

        DD<E> last = post.prev;
        x.next = post;
        x.prev = last;
        post.prev = x;
        last.next = x;
        size++;

        changed();
        return x;
    }

    /** unlinks the node from this list; partial removal only used for transferring between levels without pool involvement */
    public DD<E> detach(DD<E> i) {
        if (size == 0) {
            throw new RuntimeException("How are you going to remove " + i + " from a level with size=0");
        }
        if (i == null) throw new RuntimeException("Bag requires non-null items");
        if (i.owner() != getID()) throw new RuntimeException("Removal of non-owned item: " + i + " last on level " + id);

        if ((i == pre) || (i == post))
            throw new RuntimeException("DDList fault");

        DD<E> x = i.prev;
        DD<E> y = i.next;
        x.next = y;
        y.prev = x;
        size--;

        i.prev = i.next = null;

        changed();
        return i;
    }

    /** detaches and then returns the node to the pool; a complete removal / deletion */
    public final E remove(DD<E> i) {
        pool.put(detach(i));
        return i.item;
    }

    @Override
    public DDListIterator<E> iterator() {
        DDListIterator dd = new DDListIterator();
        dd.init(this);
        return dd;
    }


    public String toString() {
        StringBuilder s = new StringBuilder();
        for (E item : this)
            s.append(item).append(' ');
        return s.toString();
    }

    public int getID() {
        return id;
    }

    @Override
    public void forEach(Consumer<? super E> action) {

        DD<E> postSentinel = post;
        DD<E> next = getFirstNode();

        while ((next!=null) && (next!=postSentinel)) {
            E item = next.item;
            action.accept(item);
            next = next.next;
        }
    }
}
