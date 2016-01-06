//package nars.util.bag.impl.experimental;
//
//import com.google.common.base.Predicate;
//import com.google.common.collect.Iterators;
//import javolution.util.FastSet;
//import javolution.util.function.Equality;
//import nars.core.Parameters;
//import nars.logic.entity.Item;
//import nars.util.bag.Bag;
//
//import java.lang.reflect.Array;
//import java.util.*;
//
///**
// * Bag implementation which distributes items into
// * discrete levels (queues) according to priority,
// * with high-performance and multi-threaded capability
// * WARNING not tested
// *
// * DEPRECATED
// * this is effectively a levelbag with a "pending" queue wrapper for fast removal,
// * so it can be reimplemented as a LevelBag wrapper interface, or that kind of wrapper
// * interface for any Bag
// */
//@Deprecated public class GearBag<E extends Item<K>,K> extends Bag.IndexedBag<E, K> {
//
//    /**
//     * priority levels
//     */
//    public final int levels;
//
//
//
//
//    /**  mapping from key to item     */
//    public final Map<K, E> nameTable;
//
//    /** level of items    */
//    public final Level[] level;
//
//    /** outgoing queue */
//    public final Deque<E> pending;
//
//    /** pending removal set */
//    public final Set<E> toRemove;
//
//    int pendingBufferMaxSize;
//    int currentLevel;
//
//    /**
//     * defined in different bags
//     */
//    final int capacity;
//
//    /** how much to focus on each level at a time, in proportion to average expected level size */
//    float levelFocus;
//
//    /**
//     * current sum of occupied level
//     */
//    private float mass;
//
//
//    private long sizeLevels = 0;
//
//    private final static Equality fastIdentity = new Equality() {
//
//        @Override
//        public int hashCodeOf(Object t) {
//            return t.hashCode();
//        }
//
//        @Override
//        public boolean areEqual(Object t, Object t1) {
//            return t==t1;
//        }
//
//        @Override
//        public int compare(Object t, Object t1) {
//            if (t == t1) return 0;
//            return -1;
//        }
//
//    };
//
//    public GearBag(int levels, int capacity) {
//
//        this.levels = levels;
//
//        this.capacity = capacity;
//        this.pendingBufferMaxSize = (int)((capacity * 0.05)+1);
//        this.levelFocus = 1.0f;
//
//        nameTable = new WeakHashMap<K,E>(capacity);
//
//        pending = new ArrayDeque<E>();
//
//        level = (Level[]) Array.newInstance(Level.class, this.levels);
//
//        toRemove = new FastSet(fastIdentity);
//
//        for (int i = 0; i < levels; i++)
//            level[i] = new Level(i, capacity/levels);
//
//        currentLevel = levels-1;
//
//        clear();
//    }
//
//
//    public class Level extends ArrayDeque<E> {
//        private final int thisLevel;
//
//        public Level(int level, int numElements) {
//            super(numElements);
//            this.thisLevel = level;
//        }
//
//        @Override
//        public void addFirst(E e) {
//            throw new RuntimeException("Unsupported");
//        }
//
//        @Override
//        public void addLast(E e) {
//            sizeLevels++;
//            super.addLast(e);
//        }
//
//        @Override
//        public E removeFirst() {
//            E e = super.removeFirst();
//            if (e!=null)
//                sizeLevels--;
//            return e;
//        }
//
//        /** how many items to remove from this level while reloading the outgoing queue
//         * should be at least one
//         */
//        int getAttention() {
//            return (int)(( ((thisLevel)/((float)levels)) * (capacity/((float)levels)) * levelFocus) + 1f);
//        }
//
//        public float getMinPriority() {
//            if (isEmpty()) return 0;
//            float min = 1.0f;
//            for (E e : this) {
//                if (e instanceof Item) {
//                    float p = ((Item)e).getPriority();
//                    if (p < min) min = p;
//                }
//            }
//            return min;
//        }
//        public float getMaxPriority() {
//            if (isEmpty()) return 0;
//            float max = 0f;
//            for (E e : this) {
//                if (e instanceof Item) {
//                    float p = ((Item)e).getPriority();
//                    if (p > max) max = p;
//                }
//            }
//            return max;
//        }
//
//    }
//
//
//    @Override
//    public final void clear() {
//        for (int i = 0; i < levels; i++) {
//            level[i].clear();
//        }
//        nameTable.clear();
//        pending.clear();
//        toRemove.clear();
//        mass = 0;
//        sizeLevels = 0;
//    }
//
//    @Override
//    protected void index(E value) {
//        /*E oldValue = */ nameTable.put(value.name(), value);
//    }
//    @Override
//    protected E unindex(K name) {
//        E removed = nameTable.remove(name);
//        return removed;
//    }
//
//
//    /**
//     * The number of items in the bag
//     *
//     * @return The number of items
//     */
//    @Override
//    public int size() {
//
//        if (Parameters.DEBUG) {
//
//        }
//
//        int s = sizeLevels() + sizePending() - toRemove.size();
//        if (s < 0) s = 0;
//        return s;
//    }
//
//    public int sizePending() { return pending.size(); }
//    public int sizeLevels() { return (int)sizeLevels; }
//
//    @Override
//    public Set<K> keySet() {
//        return nameTable.keySet();
//    }
//
//    /**
//     * Get the average priority of Items
//     *
//     * @return The average priority of Items in the bag
//     */
//    @Override
//    public float getAveragePriority() {
//        if (size() == 0) {
//            return 0.01f;
//        }
//        float f = mass / (size());
//        if (f > 1) {
//            return 1.0f;
//        }
//        return f;
//    }
//
//
//
//
//    /**
//     * Get an Item by key
//     *
//     * @param key The key of the Item
//     * @return The Item with the given key
//     */
//    @Override
//    public E GET(final K key) {
//        return nameTable.get(key);
//    }
//
//    /** returns next item in pending queue, which if empty, reloads it */
//    protected E pop() {
//        if (size() == 0) return null;
//
//        do {
//            //fill pending buffer
//            if (pending.isEmpty()) {
//
//                int j = 0;
//                while ((pending.size() < pendingBufferMaxSize) && (sizeLevels > 0)) {
//                    Level l = level[currentLevel];
//
//                    int maxLevelItemsToRemove = l.getAttention();
//
//                    for (int n = 0; (n < maxLevelItemsToRemove) && (!l.isEmpty()); n++) {
//                        E r = l.removeFirst();
//
//                        if (toRemove.remove(r)) {
//                            onRemoved(r);
//                            continue;
//                        }
//
//                        //shuffle the order of the items, distributing across each level
//                        if ((j++) % 2 == 0)
//                            pending.addLast(r);
//                        else
//                            pending.addFirst(r);
//                    }
//
//                    currentLevel--;
//                    if (currentLevel == -1) currentLevel = levels-1; //loop
//                }
//
//            }
//            else {
//                do {
//                    E e = pending.removeFirst();
//                    if (!toRemove.remove(e)) {
//                        onRemoved(e);
//                        return e;
//                    }
//                    else {
//                        //discarding because it was in removal queue
//
//                    }
//                } while (!pending.isEmpty());
//            }
//        }
//        while (sizeLevels() > 0);
//
//        return null;
//    }
//
//
//    @Override
//    public E PEEKNEXT() {
//        if (size() == 0) return null;
//
//        E e = pop();
//        PUT(e);
//
//        return e;
//    }
//
//    @Override
//    public E TAKENEXT() {
//        return pop();
//    }
//
//    @Override public E take(final K name, boolean unindex) {
//
//        if (unindex) {
//            E oldItem = nameTable.remove(name);
//            if (oldItem == null) {
//                return null;
//            }
//            toRemove.add(oldItem);
//            return oldItem;
//        }
//        else {
//            return nameTable.get(name);
//        }
//
//    }
//
//
//
//    /**
//     * Decide the put-in level according to priority
//     *
//     * @param item The Item to put in
//     * @return The put-in level
//     */
//    private int getLevel(final E item) {
//        final float fl = item.getPriority() * levels;
//        final int level = (int) Math.ceil(fl) - 1;
//        if (level < 0) return 0;
//        if (level >= levels) return levels-1;
//        return level;
//    }
//
//
//    /**
//     * Insert an item into the itemTable, and return the overflow
//     *
//     * @param newItem The Item to put in
//     * @return null if nothing overflowed, non-null if an overflow Item, which
//     * may be the attempted input item (in which case it was not inserted)
//     */
//    @Override protected E addItem(final E e, boolean index) {
//
//        E removed = null;
//
//        if (index) {
//            if (toRemove.remove(e)) {
//                //no longer should be removed because it's added again
//            }
//        }
//
//        int s = size();
//        if (s > capacity) throw new RuntimeException("Bag exceeded capacity");
//        else if ((s == capacity) && (sizeLevels() > 0)) {
//            //remove item from lowest non-empty level
//            for (int l = 0; l < levels; l++) {
//                if (!level[l].isEmpty()) {
//                    removed = level[l].removeFirst();
//                    onRemoved(removed);
//                    break;
//                }
//            }
//            if (removed == null) throw new RuntimeException("Bag did not remove excess item");
//
//        }
//
//        int l = getLevel(e);
//        level[l].addLast(e);
//
//        if (index) {
//            nameTable.put(e.name(), e);
//        }
//
//        addMass(e);
//
//        return removed;
//    }
//
//
//    protected void removeMass(E item) {
//        mass -= item.getPriority();
//    }
//    protected void addMass(E item) {
//        mass += item.getPriority();
//    }
//
//
//
//    /**
//     * show item Table Sizes
//     * WARNING may not be working
//     */
//    public String showSizes() {
//        StringBuilder buf = new StringBuilder(" ");
//        int l = 0;
//        for (Collection<E> items : level) {
//            if ((items != null) && (!items.isEmpty())) {
//                l++;
//                buf.append(items.size()).append(' ');
//            }
//        }
//        return "Levels: " + Integer.toString(l) + ", sizes: " + buf;
//    }
//
//    @Override
//    public float getMass() {
//        return mass;
//    }
//
//    public float getAverageItemsPerLevel() {
//        return capacity / levels;
//    }
//
//
//    @Override
//    public int getCapacity() {
//        return capacity;
//    }
//
//    @Override
//    public Collection<E> values() {
//        return nameTable.values();
//    }
//
//    Iterator<E>[] ii = new Iterator[0];
//    final Predicate<E> existingElementFilter = new Predicate<E>() {
//        @Override public boolean apply(final E t) {
//            return !toRemove.contains(t);
//        }
//    };
//
//    @Override
//    public Iterator<E> iterator() {
//        if (ii.length!=levels) ii = new Iterator[levels+1];
//        ii[0] = pending.iterator();
//        for (int i = 0; i < levels; i++)
//            ii[i+1] = level[i].iterator();
//
//
//        if (toRemove.isEmpty())
//            return Iterators.concat(ii);
//        else
//            return Iterators.filter(Iterators.concat(ii), existingElementFilter);
//    }
//
//
//
////    private void stat() {
////        int itsize = 0;
////        Set<CharSequence> items = new HashSet();
////        for (Deque<E> x : itemTable) {
////            if (x != null) {
////                itsize += x.size();
////                for (E e : x)
////                    if (e!=null) items.add(e.name());
////            }
////        }
////        int nsize = nameTable.size();
////        if (nsize != itsize) {
////            System.err.println("nameTable=" + nameTable.size() + " , itemTable=" + itsize);
//////            Set<CharSequence> named = new HashSet(nameTable.keySet());
//////            if (itsize > nsize) {
//////                System.err.println("  itemTable extras: " + items.removeAll(named));
//////            }
//////            else {
//////                System.err.println("  nameTable extras: " + named.removeAll(items));
//////            }
////
////        }
////
////    }
//
//
//    /** called when an item is actuall removed from the bag */
//    protected void onRemoved(E r) {
//        removeMass(r);
//        nameTable.remove(r.name());
//    }
//
//
// }
