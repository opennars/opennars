//package nars.bag.impl.experimental;
//
//import com.google.common.base.Predicate;
//import com.google.common.collect.Iterators;
//import nars.Global;
//import nars.bag.Bag;
//import nars.bag.BagBudget;
//import nars.budget.Item;
//import nars.util.data.Util;
//import nars.util.data.list.CircularArrayList;
//import nars.util.data.map.CuckooMap;
//import org.apache.commons.math3.stat.Frequency;
//import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
//
//import java.util.Iterator;
//import java.util.Random;
//import java.util.Set;
//import java.util.function.Consumer;
//
///**
// * iteratively sorts a items by priority via a partial bubble sorted.
// *
// * EXPERIMENTAL
// *
// * runs "solid state" prioritizing bag designed to exhibit with the following properties:
// *      1. entirely pre-allocated, requires no additional allocations during activity
// *      2. O(logN) index operations (simliar to HashMap)
// *      3. O(1) takeNext, peek
// *      4. approximate target capacity above which it can grow, but within a buffer window
// *      5. priority / probability fairness
// *      6. nearly lock-free multithread access
// */
//
//public class BubbleBag<E extends Item<K>,K> extends Bag<K, E> {
//
//    int capacity;
//
//    final Random rng;
//
//    public final CuckooMap<K,E> index;
//    public final CircularArrayList<E> queue;
//
//    public final SummaryStatistics inPriority = new SummaryStatistics();
//    //public final SummaryStatistics peekPriority = new SummaryStatistics();
//    public final SummaryStatistics outPriority = new SummaryStatistics();
//    public Frequency removal = new Frequency();
//
//
//    //Deque<Bagged<E>> pool = new ArrayDeque<>();
//
//    public BubbleBag(Random rng, int initialCapacity) {
//        this(rng, initialCapacity, 0.5f);
//    }
//
//    public BubbleBag(Random rng, int initialCapacity, float loadFactor) {
//        this.rng = rng;
//        capacity = initialCapacity;
//        index = new CuckooMap(rng, capacity, loadFactor);
//        queue = new CircularArrayList<>(capacity*2 /* extra space for invalid items */ );
//
//    }
//
//    public void clearStats() {
//        inPriority.clear();
//        //peekPriority.clear();
//        outPriority.clear();
//    }
//
//    @Override
//    public void clear() {
//        clearStats();
//        index.clear();
//        queue.clear();
//    }
//
//    @Override
//    public E get(K key) {
//        return index.get(key);
//    }
//
//    @Override
//    public Set<K> keySet() {
//        //TODO provide faster accessor which is not Set<K>
//        return null;
//    }
//
//    @Override
//    public int capacity() {
//        return capacity;
//    }
//
//
//    @Override
//    public E pop() {
//        return next(true, true);
//    }
//
//    public E peekNextWithoutReordering() {
//        return next(false, true);
//    }
//
//    protected void swapToProportionalIndex(int currentIndex, float priority) {
//        //TODO choose reinsertionIndex intelligently
//        //int reinsertionIndex = Memory.randomNumber.nextInt(queue.size()-1);
//        int reinsertionIndex = (int)(rng.nextFloat() * (1.0f - priority) * (queue.size()-1));
//        if (currentIndex == reinsertionIndex) return;
//        queue.swap(currentIndex, reinsertionIndex);
//    }
//
//    @Override
//    public E peekNext() {
//        E selected = next(false, true);
//
//        swapToProportionalIndex(0, selected.getPriority());
//
//        return selected;
//    }
//
//    public final boolean contains(K k) {
//        return index.containsKey(k);
//    }
//
//    @Override
//    public final boolean contains(E it) {
//            return index.containsKey(it.name());
//    }
//
//    @Override
//    public final void forEach(Consumer<? super E> action) {
//        queue.forEach(action);
//    }
//
//    protected void remove(E item, int qi) {
//        index.remove(item.name());
//        queue.removeFast(qi); //maybe just fill with null, and we'll remove it when it reaches the end
//    }
//
//    protected E next(boolean remove, boolean highEnd) {
//        if (size() == 0) return null;
//
//        int s = queue.size();
//        int start = highEnd ? 0 : s;
//        int stop = highEnd ? s-1 : -1;
//        int inc = highEnd ? 1 : -1;
//
//        E result = null;
//        while (size() > 0) {
//            E next;
//
//            if (highEnd) {
//                next = remove ? queue.removeFirst() : queue.getFirst();
//            }
//            else {
//                next = remove ? queue.removeLast() : queue.getLast();
//            }
//
//            //see if still contained in key table, if so, it is a valid result
//            if (valid(next)) {
//                result = next;
//                if (remove) {
//                    //ACTUAL REMOVAL
//
//                    //TODO combine valid() and remove() to removeIfValid()
//                    index.remove(next.name());
//                    onExit(next);
//                }
//                break;
//            }
//            else {
//                if (!remove) {
//                    //actually remove it since we didnt do that originally
//                    if (highEnd) queue.removeFirstFast();
//                    else queue.removeLastFast();
//                }
//            }
//        }
//
//        if (result==null) {
//            //all items should have been removed, meaning the size() == 0
//            if (Global.THREADS == 1) {
//                if (size() != 0) {
//                    System.out.println(start + " " + stop);
//                    System.err.println("index=" + index);
//                    System.err.println("queue=" + queue);
//                    throw new RuntimeException("inconsistent index and queue size; queue emptied");
//                }
//            } else {
//                //multithread may have appended new items while this was traversing, may need to re-iterate
//                //??
//            }
//        }
//
//        organize();
//
//        return result;
//    }
//
//    /** tests whether a particular value is consistent (identity test)
//     *  with regard to the index's value */
//    public boolean valid(E value) {
//        return value!=null && get(value.name())==value;
//    }
//    public float validPriority(E value) {
//        if (valid(value))
//            return value.getPriority();
//        return -1.0f;
//    }
//
//    /** performs an organization iteration */
//    protected void organize() {
//
//        sortFirstAndNths(1);
//        sortFirstAndNths(2);
//        sortFirstAndNths(3);
//        sortFirstAndNths(4);
//        //sortRandomPair();
//    }
//
//    @Override
//    public String toString() {
//        return queue.toString();
//    }
//
//    @Override
//    public void setCapacity(int c) {
//        capacity = c;
//    }
//
//    /** find starting point, removing trailing invalid items */
//    protected E getValidStart() {
//        E root = null;
//        while (!queue.isEmpty()) {
//            root = queue.getFirst();
//            if (!valid(root)) {
//                queue.removeFirstFast();
//            } else
//                break;
//        }
//        return root;
//    }
//    protected E getValidEnd() {
//        E root = null;
//        while (!queue.isEmpty()) {
//            root = queue.getLast();
//            if (!valid(root)) {
//                queue.removeLastFast();
//            } else
//                break;
//        }
//        return root;
//    }
//
//    /** bubble sorted the top item with successive elements at each 'inc' interval.
//     *  if the target element is invalid, swap it and then remove the top */
//    protected void sortFirstAndNths(int inc) {
//        E root = getValidStart();
//        E end = getValidEnd();
//
//        if ((root == null) || (queue.size() < 2)) return; //nothing to do
//
//        int ai = 0;
//        for (int bi = 1; bi < queue.size(); bi += inc) {
//            E a = queue.get(ai);
//            E b = queue.get(bi);
//            float ap = validPriority(a);
//            float bp = validPriority(b);
//            if (bp > ap) {
//                queue.swap(ai, bi);
//            }
//            else {
//                //??
//                //queue.swapWithLast(i);
//            }
//
//            ai++;
//        }
//    }
//
//    /** TODO choose 2 random points and swap them if they are out of order; if one or both are invalid, swap with bottom items */
//    protected void sortRandomPair() {
//
//        if (size() >= 2) {
//
//        }
//    }
//
//
//    @Override
//    public E put(E n) {
//
//        if (n==null)
//            throw new RuntimeException(this + " can not accept null item");
//
//        E overflow;
//
//        E existing = remove(n.name());
//        if (existing!=null) {
//            //just value has changed, but doesn't matter we have removed it
//            overflow = null;
//        }
//        else if (size() >= capacity) {
//            overflow = next(true, false);
//            if (overflow == null)
//                throw new RuntimeException("overflow item not found");
//            index.remove(overflow.name());
//        }
//        else {
//            overflow = null; //capacity remains
//        }
//
//
//        onEnter(n);
//
//        return overflow;
//    }
//
//    public E onEnter(E n) {
//        float p = n.getPriority();
//        inPriority.addValue(p);
//        index.put(n.name(), n);
//        queue.addFirst(n); //TODO determine insertion policy
//        swapToProportionalIndex(0, n.getPriority());
//        return n;
//    }
//
//    public E onExit(E e) {
//        if (e == null) return null;
//
//        float p = e.getPriority();
//        outPriority.addValue(p);
//        removal.addValue(Util.decimalize(p));
//        return e;
//    }
//
//
//    @Override
//    public BagBudget<K> remove(K key) {
//        return onExit(index.remove(key));
//    }
//
//    @Override
//    public int size() {
//        //the index is the definitive authority on the size;
//        //the queue may be larger containing invalidated items pending removal
//        return index.size();
//    }
//
//    @Override
//    public Iterable<E> values() {
//        return index.v();
//    }
//
//
//
//    public Iterator<E> indexValues() {
//        return index.v().iterator();
//    }
//    public Iterator<K> indexKeys() {
//        return index.keys().iterator();
//    }
//
//    @Override
//    public Iterator<E> iterator() {
//        //TODO make a custom iterator but for now, a filter:
//        return Iterators.filter(queue.iterator(), new ValidItem());
//    }
//
//    private class ValidItem implements Predicate<E> {
//        @Override
//        public boolean apply(E e) {
//            return (valid(e));
//        }
//    }
//
//
//
//}
//
////class SolidBagBroke<E extends Item<K>,K> extends Bag<E,K> {
////
////    private int limit;
////    //Memory.randomNumber;
////
////
////
////    /** item wrapper: serves as both entry in the containment set, and in the priority queue */
////    private static class Bagged<T> {
////
////        int hash;
////        T value;
////        Bagged<T> next;
////
////        //TODO maybe store .name() key as a field for some extra speed at memory cost
////
////        void hash(final int hash, final T value) {
////            this.hash = hash;
////            this.value = value;
////        }
////
////        public void reset() {
////            this.value = null;
////            this.next = null;
////            this.hash = 0;
////        }
////
////        /*private Bagged(int hash, T key) {
////            this.hash = hash;
////            value = key;
////        }*/
////
////        @Override
////        public String toString() {
////            return "<" + value + ","+ hash + ",->" + next +">";
////        }
////    }
////
////    final static Random rng = new XORShiftRandom(1);
////    Deque<Bagged<E>> pool = new ArrayDeque<>();
////    transient Bagged<E>[] index;
////    CircularArrayList<Bagged<E>> queue;
////
////
////    SummaryStatistics returnedItemPriority = new SummaryStatistics();
////    long addCount = 0, removalCount = 0, peekCount = 0;
////    float mass = 0;
////
////    private static final int MINIMUM_CAPACITY = 4;
////    private static final int MAXIMUM_CAPACITY = 1 << 30;
////
////    private static final Bagged[] EMPTY_TABLE = new Bagged[MINIMUM_CAPACITY >>> 1];
////
////    boolean grows = false; //true allows resizing, false limits to constant capacity (see: limit field)
////
////    transient int size;
////    transient int maxCap;
////    transient int capacity;
////    private transient int threshold;
////    transient int ptr = 0;
////
////    /** capacity needs to be greater than zero, and ideally with some attention paid to power of 2 sizing as it determines termporary growth buffer room */
////    public SolidBag(int capacity) {
////
////        alloc(capacity);
////
////    }
////
////    public static int roundUpToPowerOfTwo(int i) {
////        // If input is a power of two, shift its high-order bit right.
////        i--;
////
////        // "Smear" the high-order bit all the way to the right.
////        i |= i >>> 1;
////        i |= i >>> 2;
////        i |= i >>> 4;
////        i |= i >>> 8;
////        i |= i >>> 16;
////
////        return i + 1;
////    }
////
////    public int secondaryHash(K key) {
////        int hash = key.hashCode();
////        hash ^= (hash >>> 20) ^ (hash >>> 12);
////        hash ^= (hash >>> 7) ^ (hash >>> 4);
////        return hash;
////    }
////
////    @Override
////    public int size() {
////        return size;
////    }
////
////
////    @Override
////    public float getAveragePriority() {
////        return 0;
////    }
////
////
////    @Override
////    public E take(K key, boolean unindex) {
////        if (size == 0) return null;
////
////        int hash = secondaryHash(key);
////        Bagged<E>[] tab = index;
////        int index = hash & (tab.length - 1);
////        for (Bagged<E> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
////            if (e.hash == hash && key.equals(e.value.name())) {
////                if (prev == null) {
////                    tab[index] = e.next;
////                } else {
////                    prev.next = e.next;
////                }
////                size--;
////                E v = e.value;
////                e.reset();
////                pool.addLast(e);
////                return v;
////            }
////        }
////        return null;
////    }
////
////    @Override
////    public E take(K key) {
////        return take(key, true);
////    }
////
////    @Override
////    public E take(E value) {
////        return take(value.name(), true);
////    }
////
////    @Override
////    public E takeNext() {
////        Bagged<E> b = peekNextBagged();
////        if (b == null) return null;
////
////        if (b.value == null)
////            throw new RuntimeException("takeNext bagged fail");
////
////        System.out.println("taking: " + b + " " + b.value + " " + contains(b.value.name()));
////        return take(b.value);
////    }
////
////    protected Bagged<E> peekNextBagged() {
////        if (size == 0) return null;
////        //int random = rng.nextInt(queue.size());
////
////        final int qs = queue.size();
////        do {
////            ptr--;
////            if (ptr < 0) ptr = qs-1;
////
////            Bagged<E> e = queue.getModulo(ptr);
////            if ((e!=null) && (e.value!=null))
////                return e;
////        }
////        while (true);
////
////    }
////
////    @Override
////    public E peekNext() {
////        Bagged<E> b = peekNextBagged();
////        return b.value;
////    }
////
////
////    @Override
////    public synchronized E putIn(E newItem) {
////        return addItem(newItem, true);
////    }
////
////    @Override
////    protected synchronized E addItem(E item, boolean _indexIt) {
////        K key = item.name();
////
////        System.out.println("add: " + item + " " + pool.size());
////
////        int hash = secondaryHash(key);
////        Bagged<E>[] tab = index;
////        int ndex = hash & (tab.length - 1);
////        for (Bagged<E> e = tab[ndex]; e != null; e = e.next) {
////            if (e.value == key || (e.hash == hash && e.value.equals(key))) {
////                //already present
////
////                //System.out.println("already exists: " + item);
////                return null;
////            }
////        }
////
////        //new item
////
////
////        E overflow = null;
////
////        if (size >= limit) {
////            overflow = takeNext();
////            if (overflow == null) {
////                print(System.err);
////                throw new RuntimeException("overflow fail");
////            }
////        }
////        else {
////        }
////
////
////
////        size++;
////
////        // No entry for (non-null) key is present; create one
////        if ((size >= threshold) && (grows)) {
////
////            //reallocate
////            tab = doubleCapacity();
////
////            //recompute hash for this item
////            ndex = hash & (tab.length - 1);
////        }
////
////        if (pool.isEmpty())
////            throw new RuntimeException(this + " pool fault while adding " + item + " with size=" + size + " capacity=" + capacity + " maxCap=" + maxCap);
////
////        Bagged<E> p = pool.removeFirst();
////
////        p.hash(hash, item);
////
////        queue(p);
////
////        tab[ndex] = p;
////
////        return overflow;
////    }
////
////    protected void queue(Bagged<E> b) {
////        //if priority > 0.5 insert at top
////        //if priority <= 0.5 insert at bottom
////        System.out.println(" q> " + b);
////        //if (b.value.getPriority() < 0.5)
////            queue.addLast(b);
////        //else
////          //  queue.addFirst(b);
////    }
////
////
////    /**
////     * Allocate a table of the given capacity and set the threshold accordingly.
////     *
////     * @param newCapacity must be a power of two
////     */
////    private Bagged<E>[] alloc(int newCapacity) {
////
////        this.limit = newCapacity;
////
////        int maxCap = newCapacity;
////
////        if (maxCap < MINIMUM_CAPACITY) {
////            maxCap = MINIMUM_CAPACITY;
////        } else if (maxCap > MAXIMUM_CAPACITY) {
////            maxCap = MAXIMUM_CAPACITY;
////        } else {
////            maxCap = roundUpToPowerOfTwo(maxCap);
////        }
////
////        @SuppressWarnings("unchecked")
////        Bagged<E>[] newTable = (Bagged<E>[]) new Bagged[newCapacity];
////        index = newTable;
////        threshold = (newCapacity >> 1) + (newCapacity >> 2); // 3/4 capacity
////
////
////        this.capacity = newCapacity;
////        this.maxCap = maxCap;
////
////        for (int i = 0; i < limit; i++) {
////            pool.addLast(new Bagged()); //only allowed to be allocated here
////        }
////
////        this.queue = new CircularArrayList(limit);
////
////
////        return newTable;
////    }
////
////    /**
////     * Doubles the capacity of the hash table. Existing entries are placed in
////     * the correct bucket on the enlarged table. If the current capacity is,
////     * MAXIMUM_CAPACITY, this method is a no-op. Returns the table, which
////     * will be new unless we were already at MAXIMUM_CAPACITY.
////     */
////    private Bagged<E>[] doubleCapacity() {
////        Bagged<E>[] oldTable = index;
////        int oldCapacity = oldTable.length;
////        if (oldCapacity == MAXIMUM_CAPACITY) {
////            return oldTable;
////        }
////        int newCapacity = oldCapacity * 2;
////        Bagged<E>[] newTable = alloc(newCapacity);
////        if (size == 0) {
////            return newTable;
////        }
////
////        for (int j = 0; j < oldCapacity; j++) {
////            /*
////             * Rehash the bucket using the minimum number of field writes.
////             * This is the most subtle and delicate code in the class.
////             */
////            Bagged<E> e = oldTable[j];
////            if (e == null) {
////                continue;
////            }
////            int highBit = e.hash & oldCapacity;
////            Bagged<E> broken = null;
////            newTable[j | highBit] = e;
////            for (Bagged<E> n = e.next; n != null; e = n, n = n.next) {
////                int nextHighBit = n.hash & oldCapacity;
////                if (nextHighBit != highBit) {
////                    if (broken == null) {
////                        newTable[j | nextHighBit] = n;
////                    } else {
////                        broken.next = n;
////                    }
////                    broken = e;
////                    highBit = nextHighBit;
////                }
////            }
////            if (broken != null)
////                broken.next = null;
////        }
////        return newTable;
////    }
////
////
////    public boolean contains(K key) {
////        return get(key)!=null;
////    }
////
////    public Bagged<E> getBagged(K key) {
////        int hash = secondaryHash(key);
////        Bagged<E>[] tab = index;
////        for (Bagged<E> e = tab[hash & (tab.length - 1)]; e != null; e = e.next) {
////            if (e.value == key || (e.hash == hash && e.value.name().equals(key))) {
////                return e;
////            }
////        }
////        return null;
////    }
////
////    @Override
////    public E get(K key) {
////        Bagged<E> b = getBagged(key);
////        if (b!=null)
////            return b.value;
////        return null;
////    }
////
////    @Override
////    public void clear() {
////        throw new RuntimeException("Not impl yet");
//////        if (size != 0) {
//////            Arrays.fill(index, null);
//////            mEntryForNull = null;
//////            size = 0;
//////        }
////    }
////
////
////    public void print(PrintStream out) {
////        Iterator<E> d = iterator();
////        out.println(size() + ": " );
////        while (d.hasNext()) {
////            E v = d.next();
////            if (v == null)
////                out.println("  " + null);
////            else
////                out.println("  " + getBagged(v.name()));
////        }
////    }
////
////    @Override
////    public Set<K> keySet() {
////        //return this;
////        return null;
////    }
////
////    @Override
////    public int getCapacity() {
////        return limit;
////    }
////
////    @Override
////    public float getMass() {
////        return mass;
////    }
////
////
////
////    @Override
////    protected void index(E value) {
////
////    }
////
////    @Override
////    protected E unindex(K key) {
////        return null;
////    }
////
////
////    public Iterator<E> hashIterator() {
////        return new HashSetIterator();
////    }
////
////    /** iterates in queue's current order, which will be approximately sorted */
////    @Override
////    public Iterator<E> iterator() {
////        return new HashSetIterator();
////
////        //return new ForwardQueueIterator();
////
////    }
////
////    public Iterator<E> descendingIterator() {
////        return new ReverseQueueIterator();
////    }
////
////    private final class ForwardQueueIterator implements Iterator<E> {
////        int pos = 0;
////        final int max = queue.size()-1;
////        @Override public boolean hasNext() {
////            return pos < max;
////        }
////        @Override public E next() { return queue.get(pos++).value;        }
////    }
////    private final class ReverseQueueIterator implements Iterator<E> {
////        int pos = queue.size()-1;
////        @Override public boolean hasNext() { return pos >0;         }
////        @Override public E next() { return queue.get(pos--).value;         }
////    }
////
////    private final class HashSetIterator implements Iterator<E> {
////
////        int nextIndex;
////        Bagged<E> nextEntry = null;
////        Bagged<E> lastEntryReturned;
////
////        private HashSetIterator() {
////
////
////
////            Bagged<E>[] tab = index;
////            Bagged<E> next = null;
////            while (next == null && nextIndex < tab.length) {
////                next = tab[nextIndex++];
////            }
////            nextEntry = next;
////        }
////
////        @Override
////        public boolean hasNext() {
////            return nextEntry != null;
////        }
////
////        @Override
////        public E next() {
////
////            if (nextEntry == null) {
////                throw new NoSuchElementException();
////            }
////
////            Bagged<E> entryToReturn = nextEntry;
////            Bagged<E>[] tab = index;
////            Bagged<E> next = entryToReturn.next;
////            while (next == null && nextIndex < tab.length) {
////                next = tab[nextIndex++];
////            }
////            nextEntry = next;
////            lastEntryReturned = entryToReturn;
////            return entryToReturn.value;
////        }
////
////        @Override
////        public void remove() {
////            throw new RuntimeException("not impl yet");
//////            if (lastEntryReturned == null) {
//////                throw new IllegalStateException();
//////            }
//////            SolidBag.this.remove(lastEntryReturned.mKey);
//////            lastEntryReturned = null;
////        }
////    }
////
////    @Override
////    public Iterable<E> values() {
////        return this;
////    }
// //}
