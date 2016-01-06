//package nars.bag.impl;
//
//import com.gs.collections.api.block.procedure.Procedure2;
//import nars.Global;
//import nars.bag.Bag;
//import nars.bag.BagBudget;
//import nars.budget.Budget;
//import nars.budget.Item;
//import nars.util.ArraySortedIndex;
//import nars.util.CollectorMap;
//import nars.util.data.list.CircularArrayList;
//import nars.util.data.sorted.SortedIndex;
//
//import java.util.*;
//import java.util.function.Consumer;
//
///**
// * Uses iterative heapsort to maintain a partially sorted
// * array of items,
// *
// * @see http://techmightsolutions.blogspot.com/2012/04/heap-sort-with-partial-heaping-in-java.html
// */
//public class HeapBag<K, E extends Item<K>> extends Bag<K, E> {
//
//    /**
//     * mapping from key to item
//     */
//    private final HeapMap index;
//
//    /**
//     * array of lists of items, for items on different level
//     */
//    public final CircularArrayList<E> items;
//
//    /**
//     * defined in different bags
//     */
//    int capacity;
//    private final Random rng;
//
//    private final CurveBag.BagCurve curve;
//
//    //TODO make these proportional to capacity
//    //sort iterations
//    final int sortPrecision = 16;
//    //# of max moves per iteration before ending it
//    final int sortMovesMax = 4;
//
//
//    public static <E extends Item> SortedIndex<E> getIndex(int capacity) {
//        //if (capacity < 50)
//        return new ArraySortedIndex(capacity);
//        //else
//        //    return new FractalSortedItemList<E>();
//    }
//
//    public HeapBag(Random rng, int capacity) {
//        this(rng, capacity, CurveBag.power6BagCurve);
//    }
//
//
//    protected class HeapMap extends CollectorMap<K, E> {
//
//        public HeapMap(int capacity) {
//            //super(new CuckooMap(rng, capacity + 1));
//            super(Global.newHashMap(capacity+1));
//        }
//
//        public HeapMap(Map<K, E> map) {
//            super(map);
//        }
//
//
//        @Override
//        public Procedure2<Budget, Budget> getMerge() {
//            return mergeFunction;
//        }
//
//        @Override
//        public E remove(K key) {
//            E e = super.remove(key);
//
//            if (Global.DEBUG)
//                HeapBag.this.size();
//
//            return e;
//        }
//
//        @Override
//        protected E removeItem(E removed) {
//            if (items.remove(removed)) {
//                return removed;
//            }
//            return null;
//        }
//
//
//
//        @Override protected E addItem(E i) {
//            E removed = null;
//            if (items.size() + 1 > capacity()) {
//                removed = items.removeLast();
//            }
//            items.addFirst(i);
//
//
//            for (int j = 0; j < sortPrecision; j++)
//                sortNext(sortMovesMax);
//
//            return removed;
//        }
//    }
//
//
//
//
//    int heapCycle;
//    int cmps = 0, movs = 0; //variables to keep track of comparisons and moves
//    //int maxHeapMovesPerCycle
//
//    /**
//     * true if: item(a) "<" item(b)
//     */
//    public boolean less(int a, int b) {
//        E B = items.get(b);
//        return less(a, B);
//    }
//
//    public boolean less(int a, E B) {
//        cmps++;
//
//        E A = items.get(a);
//
//        if (A == null)
//            throw new RuntimeException("null");
//
//        float ap = A.getPriority();
//        float bp = B.getPriority();
//
//        if (ap == bp) {
//            float aq = A.getQuality() + A.getDurability();
//            float bq = B.getQuality() + B.getDurability();
//
//            if (aq == bq) {
//                return A.hashCode() < B.hashCode();
//            }
//            return aq < bq;
//        } else {
//            return ap < bp;
//        }
//    }
//
//    public void reHeap(int k, int maxMoves) {
//
//        if (movs >= maxMoves)
//            return;
//
//        int k2 = 2 * k;
//
//        int limit = size() - 1;
//
//        if (k2 + 1 > limit)
//            return;
//
//        if (k2 + 2 > limit) {
//            if (less(k, k2 + 1)) {
//                swap(k, k2 + 1);
//                reHeap(k2 + 1, maxMoves);
//            }
//            return;
//        }
//
//        E K = items.get(k);
//        if (!less(k2 + 1, K) || !less(k2 + 2, K)) {
//
//            int t = !less(k2 + 1, k2 + 2) ? 1 : 2;
//
//            swap(k, k2 + t);
//            reHeap(k2 + t, maxMoves);
//        }
//    }
//
//    private void swap(int x, int y) {
//        items.swap(x, y);
//        movs++;
//    }
//
//    /** perform the next partial sorting iteration */
//    public void sortNext(int maxMoves) {
//        int is = items.size();
//        if (is < 2) {
//            heapCycle = 0;
//            return;
//        }
//
//        int hc = (heapCycle == 0) || (heapCycle > (is - 1)) ? (heapCycle = is - 1) : 0;
//        reHeap(hc, maxMoves);
//        swap(0, heapCycle);
//
//        heapCycle--;
//
//        //System.out.println("sortNext: " + is + " size, " + cmps + " compares, " + movs + " moves");
//
//        cmps = movs = 0;
//
//    }
//
//
//    public HeapBag(Random rng, int capacity, CurveBag.BagCurve curve) {
//        this.rng = rng;
//        this.capacity = capacity;
//        this.curve = curve;
//
//        items = new CircularArrayList<>(capacity);
//
//        index = new HeapMap(capacity);
//
//    }
//
//
//    @Override
//    public final void clear() {
//        items.clear();
//        index.clear();
//    }
//
//    /**
//     * The number of items in the bag
//     *
//     * @return The number of items
//     */
//    @Override
//    public final int size() {
//
//        int in = index.size();
//
//        if (Global.DEBUG) {
//            int is = items.size();
//
//            //test for a discrepency of +1/-1 difference between name and items
//            if ((is - in > 2) || (is - in < -2)) {
//                System.err.println(getClass() + " inconsistent index: items=" + is + " names=" + in);
//                /*System.out.println(nameTable);
//                System.out.println(items);
//                if (is > in) {
//                    List<E> e = new ArrayList(items);
//                    for (E f : nameTable.values())
//                        e.remove(f);
//                    System.out.println("difference: " + e);
//                }*/
//                throw new RuntimeException(getClass() + " inconsistent index: items=" + is + " names=" + in);
//            }
//        }
//
//        return in;
//    }
//
//
//
//
//
//
////    protected E unindex(K name) {
////        E removed = nameTable.removeKey(name);
////        return removed;
////    }
//
//
//    /**
//     * Check if an item is in the bag
//     *
//     * @param it An item
//     * @return Whether the Item is in the Bag
//     */
//    @Override
//    public boolean contains(E it) {
//        return index.containsValue(it);
//    }
//
//    /**
//     * Get an Item by key
//     *
//     * @param key The key of the Item
//     * @return The Item with the given key
//     */
//    @Override
//    public E get(K key) {
//        return index.get(key);
//    }
//
//    @Override
//    public BagBudget<K> remove(K key) {
//        return index.remove(key);
//    }
//
//
//    /**
//     * Choose an Item according to priority distribution and take it out of the
//     * Bag
//     *
//     * @return The selected Item, or null if this bag is empty
//     */
//    @Override
//    public final E pop() {
//
//        if (isEmpty()) return null; // empty bag
//        return removeItem(nextRemovalIndex());
//
//    }
//
//
//    @Override
//    public final E peekNext() {
//
//        if (isEmpty()) return null; // empty bag
//        return items.get(nextRemovalIndex());
//
//    }
//
//
//    /**
//     * distributor function
//     */
//    public int nextRemovalIndex() {
//        int s = items.size();
//
//        float x = rng.nextFloat();
//
//        float y = curve.valueOf(x);
//
//        int result = (int)Math.floor(y * s);
//
//        if (result < 0) return 0;
//        if (result >= s) return s-1;
//        return result;
//    }
//
//    //    public static long fastRound(final double d) {
////        if (d > 0) {
////            return (long) (d + 0.5d);
////        } else {
////            return (long) (d - 0.5d);
////        }
////    }
////
//
//
//    @Override
//    public boolean isEmpty() {
//        return items.isEmpty();
//    }
//
//    @Override
//    public float getPriorityMin() {
//        if (items.isEmpty()) return 0;
//        return items.getFirst().getPriority();
//    }
//
//    @Override
//    public float getPriorityMax() {
//        if (items.isEmpty()) return 0;
//        return items.getLast().getPriority();
//    }
//
//    /**
//     * Insert an item into the itemTable, and return the overflow
//     *
//     * @param i The Item to put in
//     * @return The overflow Item, or null if nothing displaced
//     */
//    @Override
//    public E put(E i) {
//
//
//        float newPriority = i.getPriority();
//
//        boolean contains = index.containsKey(i.name());
//        if ((index.size() >= capacity) && (!contains)) {
//            // the bag is full
//
//            // this item is below the bag's already minimum item, no change (return the input as the overflow)
//            if (newPriority < getPriorityMin()) {
//                return i;
//            }
//
//            E oldItem = removeItem(0);
//
//            if (oldItem == null)
//                throw new RuntimeException("required removal but nothing removed");
//            if (oldItem.name().equals(i.name())) {
//                throw new RuntimeException("this oldItem should have been removed on earlier nameTable.put call: " + oldItem + ", during put(" + i + ')');
//            }
//
//
//            //insert
//            index.put(i);
//
//
//
//            return oldItem;
//        } else if (contains) {
//            //TODO check this mass calculation
//            /*E existingToReplace = */
//            index.put(i);
//
//            return null;
//        } else /* if (!contains) */ {
//            E shouldNotExist = index.put(i);
//            if (shouldNotExist != null)
//                throw new RuntimeException("already expected no existing key/item but it was actually there");
//
//            return null;
//        }
//
//
//    }
//
////
////    protected synchronized E removeItem2(final int index) {
////
////        final E selected;
////
////        selected = items.remove(index);
////        if (selected != null) {
////            E removed = nameTable.removeKey(selected.name());
////
////            if (removed == null)
////                throw new RuntimeException(this + " inconsistent index: items contained " + selected + " but had no key referencing it");
////
////            //should be the same object instance
////            if ((removed != null) && (removed != selected)) {
////                throw new RuntimeException(this + " inconsistent index: items contained " + selected + " and index referenced " + removed + " + ");
////            }
////            mass -= selected.budget.getPriority();
////        } else {
////            throw new RuntimeException(this + " items array returned null item at index " + index);
////        }
////
////        return selected;
////    }
//
//
//    /**
//     * Take out the first or last E in a level from the itemTable
//     *
//     * @param level The current level
//     * @return The first Item
//     */
//    protected E removeItem(int index) {
//
//        /*synchronized (nameTable)*/
//
//        E selected = items.remove(index);
//        if (selected == null)
//            throw new RuntimeException(this + " inconsistent index: items contained #" + index + " but had no key referencing it");
//
//        //should be the same object instance
//        this.index.removeKey(selected.name());
//
//        return selected;
//    }
//
//
//    //TODO handle deleted items like Bag.update(..)
////    @Override
////    public E update(BagTransaction<K, E> selector) {
////
////        final K key = selector.name();
////        final E b;
////        if (key != null) {
////            b = index.get(key);
////        }
////        else {
////            b = peekNext();
////        }
////
////
////        if (b == null) {
////            //allow selector to provide a new instance
////            E n = selector.newItem();
////            if (n!=null) {
////                return putReplacing(n, selector);
////            }
////            //no instance provided, nothing to do
////            return null;
////        }
////        else  {
////            //allow selector to modify it, then if it returns non-null, reinsert
////            temp.budget( b.getBudget() );
////
////            final Budget c = selector.updateItem(b, temp);
////
////            if ((c!=null) && (!c.equalsByPrecision(b))) {
////                b.getBudget().budget(c);
////                return putReplacing(b, selector);
////            }
////            else
////                return b;
////        }
////
////
////    }
//
//    @Override
//    public int capacity() {
//        return capacity;
//    }
//
//    @Override
//    public String toString() {
//
//        return super.toString() + '{' + items.getClass().getSimpleName() + '}';
//    }
//
//    @Override
//    public Set<K> keySet() {
//        return index.keySet();
//    }
//
//    @Override
//    public Collection<E> values() {
//        return index.values();
//    }
//
//    @Override
//    public Iterator<E> iterator() {
//        return items.descendingIterator();
//    }
//
//    @Override
//    public void forEach(Consumer<? super E> action) {
//        items.forEach(action);
//    }
//
//    @Override
//    public void setCapacity(int c) {
//        capacity = c;
//    }
//
// }
