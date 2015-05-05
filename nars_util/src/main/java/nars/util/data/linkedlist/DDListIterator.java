package nars.util.data.linkedlist;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
    note: assumes that no calls to DDList.add() will be made during iteration,
    so it is probably only safe in non-concurrent cases
*/
public class DDListIterator<E> implements Iterator<E> {

    private DD<E> current;  // the node that is returned by next()
    //private DDNodePool.DD<E> lastAccessed;      // the last node to be returned by prev() or next()
    // reset to null upon intervening remove() or add()
    private int index;
    private int size;

    public void init(DDList<E> d) {
        current = d.pre.next;
        //lastAccessed = null;
        index = 0;
        size = d.size;
    }
    @Override
    public boolean hasNext() {
        return index < size;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public int previousIndex() {
        return index - 1;
    }

    public int nextIndex() {
        return index;
    }

    @Override
    public E next() {
        if (!hasNext()) throw new NoSuchElementException();
        //lastAccessed = current;
        E item = current.item;
        current = current.next;
        index++;
        return item;
    }

    public E previous() {
        if (!hasPrevious()) throw new NoSuchElementException();
        current = current.prev;
        index--;
        //lastAccessed = current;
        return current.item;
    }

//            // replace the item of the element that was last accessed by next() or previous()
//            // condition: no calls to remove() or add() after last call to next() or previous()
//            public void set(E item) {
//                if (lastAccessed == null) throw new IllegalStateException();
//                lastAccessed.item = item;
//                changed();
//            }
//
//
//            // remove the element that was last accessed by next() or previous()
//            // condition: no calls to remove() or add() after last call to next() or previous()
//            public void remove() {
//                changed();
//                throw new RuntimeException("not fully implemented");
//                /*
//                if (lastAccessed == null) throw new IllegalStateException();
//                DDNode x = lastAccessed.prev;
//                DDNode y = lastAccessed.next;
//                x.next = y;
//                y.prev = x;
//                N--;
//                if (current == lastAccessed)
//                    current = y;
//                else
//                    index--;
//                lastAccessed = null;
//                */
//            }
//
//
//            // add element to list
//            public void add(E item) {
//                changed();
//                throw new RuntimeException("not fully implemented");
//                /*
//                DDNode x = current.prev;
//                DDNode y = pool.get();
//                DDNode z = current;
//                y.item = item;
//                x.next = y;
//                y.next = z;
//                z.prev = y;
//                y.prev = x;
//                N++;
//                index++;
//                lastAccessed = null;
//                */
//            }
//

}
