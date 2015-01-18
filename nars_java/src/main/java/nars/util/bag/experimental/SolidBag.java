package nars.util.bag.experimental;

import nars.logic.entity.Item;
import nars.util.bag.Bag;
import nars.util.data.CircularArrayList;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/** "solid state" prioritizing bag designed to exhibit with the following properties:
 *      1. entirely pre-allocated, requires no additional allocations during activity
 *      2. O(logN) index operations (simliar to HashMap)
 *      3. O(1) takeNext, peek
 *      4. approximate target capacity above which it can grow, but within a buffer window
 *      5. priority / probability fairness
 *      6. nearly lock-free multithread access
 */
public class SolidBag<E extends Item<K>,K> extends Bag<E,K> {

    /** item wrapper: serves as both entry in the containment set, and in the priority queue */
    private static class Bagged<T> {

        int hash;
        T value;
        Bagged<T> next;

        void hash(final int hash, final T value) {
            this.hash = hash;
            this.value = value;
        }

        /*private Bagged(int hash, T key) {
            this.hash = hash;
            value = key;
        }*/

    }

    transient Bagged<E>[] index;
    CircularArrayList<Bagged<E>> queue;
    ConcurrentLinkedQueue<Bagged<E>> pool = new ConcurrentLinkedQueue();


    SummaryStatistics returnedItemPriority = new SummaryStatistics();
    long addCount = 0, removalCount = 0, peekCount = 0;
    float mass = 0;

    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private static final Bagged[] EMPTY_TABLE = new Bagged[MINIMUM_CAPACITY >>> 1];

    transient int size;
    transient int maxCap;
    transient int capacity;
    private transient int threshold;

    /** capacity needs to be greater than zero, and ideally with some attention paid to power of 2 sizing as it determines termporary growth buffer room */
    public SolidBag(int capacity) {

        alloc(capacity);

    }

    public static int roundUpToPowerOfTwo(int i) {
        // If input is a power of two, shift its high-order bit right.
        i--;

        // "Smear" the high-order bit all the way to the right.
        i |= i >>> 1;
        i |= i >>> 2;
        i |= i >>> 4;
        i |= i >>> 8;
        i |= i >>> 16;

        return i + 1;
    }

    public int secondaryHash(K key) {
        int hash = key.hashCode();
        hash ^= (hash >>> 20) ^ (hash >>> 12);
        hash ^= (hash >>> 7) ^ (hash >>> 4);
        return hash;
    }

    @Override
    public int size() {
        return size;
    }


    @Override
    public float getAveragePriority() {
        return 0;
    }


    @Override
    public E take(K key, boolean unindex) {

        int hash = secondaryHash(key);
        Bagged<E>[] tab = index;
        int index = hash & (tab.length - 1);
        for (Bagged<E> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
            if (e.hash == hash && key.equals(e.value.name())) {
                if (prev == null) {
                    tab[index] = e.next;
                } else {
                    prev.next = e.next;
                }
                size--;
                pool.offer(e);
                return e.value;
            }
        }
        return null;
    }


    @Override
    public E takeNext() {
        return null;
    }

    @Override
    public E peekNext() {
        return null;
    }


    @Override
    protected E addItem(E item, boolean _indexIt) {
        K key = item.name();

        int hash = secondaryHash(key);
        Bagged<E>[] tab = index;
        int ndex = hash & (tab.length - 1);
        for (Bagged<E> e = tab[ndex]; e != null; e = e.next) {
            if (e.value == key || (e.hash == hash && e.value.equals(key))) {
                //already present
                return null;
            }
        }

        //new item

        //TODO capacity manage
        // No entry for (non-null) key is present; create one
        if (size++ > threshold) {
            tab = doubleCapacity();
            ndex = hash & (tab.length - 1);
        }

        Bagged<E> p = pool.poll();
        if (p == null)
            throw new RuntimeException(this + " pool fault while adding " + item + " with size=" + size + " capacity=" + capacity + " maxCap=" + maxCap);

        p.hash(hash, item);

        queue(p);

        tab[ndex] = p;

        E displaced = null;
        return displaced;
    }

    protected void queue(Bagged<E> b) {
        //if priority > 0.5 insert at top
        //if priority <= 0.5 insert at bottom
        if (b.value.getPriority() < 0.5)
            queue.addLast(b);
        else
            queue.addFirst(b);
    }


    /**
     * Allocate a table of the given capacity and set the threshold accordingly.
     *
     * @param newCapacity must be a power of two
     */
    private Bagged<E>[] alloc(int newCapacity) {

        int maxCap = newCapacity;

        if (maxCap < MINIMUM_CAPACITY) {
            maxCap = MINIMUM_CAPACITY;
        } else if (maxCap > MAXIMUM_CAPACITY) {
            maxCap = MAXIMUM_CAPACITY;
        } else {
            maxCap = roundUpToPowerOfTwo(maxCap);
        }


        @SuppressWarnings("unchecked")
        Bagged<E>[] newTable = (Bagged<E>[]) new Bagged[newCapacity];
        index = newTable;
        threshold = (newCapacity >> 1) + (newCapacity >> 2); // 3/4 capacity


        this.capacity = newCapacity;
        this.maxCap = maxCap;

        for (int i = 0; i < maxCap; i++) {
            pool.offer(new Bagged());
        }

        this.queue = new CircularArrayList(maxCap);

        return newTable;
    }

    /**
     * Doubles the capacity of the hash table. Existing entries are placed in
     * the correct bucket on the enlarged table. If the current capacity is,
     * MAXIMUM_CAPACITY, this method is a no-op. Returns the table, which
     * will be new unless we were already at MAXIMUM_CAPACITY.
     */
    private Bagged<E>[] doubleCapacity() {
        Bagged<E>[] oldTable = index;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            return oldTable;
        }
        int newCapacity = oldCapacity * 2;
        Bagged<E>[] newTable = alloc(newCapacity);
        if (size == 0) {
            return newTable;
        }

        for (int j = 0; j < oldCapacity; j++) {
            /*
             * Rehash the bucket using the minimum number of field writes.
             * This is the most subtle and delicate code in the class.
             */
            Bagged<E> e = oldTable[j];
            if (e == null) {
                continue;
            }
            int highBit = e.hash & oldCapacity;
            Bagged<E> broken = null;
            newTable[j | highBit] = e;
            for (Bagged<E> n = e.next; n != null; e = n, n = n.next) {
                int nextHighBit = n.hash & oldCapacity;
                if (nextHighBit != highBit) {
                    if (broken == null) {
                        newTable[j | nextHighBit] = n;
                    } else {
                        broken.next = n;
                    }
                    broken = e;
                    highBit = nextHighBit;
                }
            }
            if (broken != null)
                broken.next = null;
        }
        return newTable;
    }


    public boolean contains(K key) {
        return get(key)!=null;
    }

    @Override
    public E get(K key) {
        int hash = secondaryHash(key);
        Bagged<E>[] tab = index;
        for (Bagged<E> e = tab[hash & (tab.length - 1)]; e != null; e = e.next) {
            if (e.value == key || (e.hash == hash && e.value.name().equals(key))) {
                return e.value;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        throw new RuntimeException("Not impl yet");
//        if (size != 0) {
//            Arrays.fill(index, null);
//            mEntryForNull = null;
//            size = 0;
//        }
    }


    public void print(PrintStream out) {
        Iterator<E> d = descendingIterator();
        while (d.hasNext()) {
            out.println(d.next());
        }
    }

    @Override
    public Set<K> keySet() {
        //return this;
        return null;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public float getMass() {
        return mass;
    }



    @Override
    protected void index(E value) {

    }

    @Override
    protected E unindex(K key) {
        return null;
    }


    public Iterator<E> hashIterator() {
        return new HashSetIterator();
    }

    /** iterates in queue's current order, which will be approximately sorted */
    @Override
    public Iterator<E> iterator() {
        return new ForwardQueueIterator();
    }

    public Iterator<E> descendingIterator() {
        return new ReverseQueueIterator();
    }

    private final class ForwardQueueIterator implements Iterator<E> {
        int pos = 0;
        final int max = queue.size()-1;
        @Override public boolean hasNext() { return pos < max;        }
        @Override public E next() { return queue.get(pos++).value;        }
    }
    private final class ReverseQueueIterator implements Iterator<E> {
        int pos = queue.size()-1;
        @Override public boolean hasNext() { return pos >=0;         }
        @Override public E next() { return queue.get(pos--).value;         }
    }

    private final class HashSetIterator implements Iterator<E> {

        int nextIndex;
        Bagged<E> nextEntry = null;
        Bagged<E> lastEntryReturned;

        private HashSetIterator() {



            Bagged<E>[] tab = index;
            Bagged<E> next = null;
            while (next == null && nextIndex < tab.length) {
                next = tab[nextIndex++];
            }
            nextEntry = next;
        }

        @Override
        public boolean hasNext() {
            return nextEntry != null;
        }

        @Override
        public E next() {

            if (nextEntry == null) {
                throw new NoSuchElementException();
            }

            Bagged<E> entryToReturn = nextEntry;
            Bagged<E>[] tab = index;
            Bagged<E> next = entryToReturn.next;
            while (next == null && nextIndex < tab.length) {
                next = tab[nextIndex++];
            }
            nextEntry = next;
            lastEntryReturned = entryToReturn;
            return entryToReturn.value;
        }

        @Override
        public void remove() {
            throw new RuntimeException("not impl yet");
//            if (lastEntryReturned == null) {
//                throw new IllegalStateException();
//            }
//            SolidBag.this.remove(lastEntryReturned.mKey);
//            lastEntryReturned = null;
        }
    }

    @Override
    public Iterable<E> values() {
        return this;
    }
}
