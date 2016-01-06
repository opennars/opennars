package nars.bag.impl;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import nars.bag.BLink;
import nars.bag.Bag;
import nars.budget.*;
import nars.util.ArraySortedIndex;
import nars.util.CollectorMap;
import nars.util.data.sorted.SortedIndex;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A bag implemented as a combination of a Map and a SortedArrayList
 */
public class ArrayBag<V> extends Bag<V> {

    //public static final Procedure2<Budget, Budget> DEFAULT_MERGE_METHOD = UnitBudget.average;

    /**
     * mapping from key to item
     */
    public final ArrayMapping index;
    /**
     * array of lists of items, for items on different level
     */
    public final SortedIndex<BLink<V>> items;


    public ArrayBag(SortedIndex<BLink<V>> items) {
        this(items,
            //Global.newHashMap(items.capacity()/2)
            new HashMap(items.capacity()/2)
        );
    }

    public ArrayBag(SortedIndex<BLink<V>> items, Map<V, BLink<V>> map) {

        items.clear();

        this.items = items;
        index = new ArrayMapping(map, items);
    }

    public ArrayBag(int capacity) {
        this(new ArraySortedIndex<>(capacity));
    }

    @Override public BLink<V> put(Object v) {
        //TODO combine with CurveBag.put(v)
        BLink<V> existing = get(v);
        if (existing!=null) {
            merge(existing.getBudget(),
                    getDefaultBudget(v), 1f);
            return existing;
        } else {
            return existing != null ? existing :
                    put((V) v, getDefaultBudget(v));
        }
    }

    private Budget getDefaultBudget(Object v) {
        if (v instanceof Budgeted)
            return ((Budgeted)v).getBudget();
        return UnitBudget.zero;
    }


    public BLink<V> put(BudgetedHandle k) {
        return put(k, k.getBudget());
    }


//    @Override public V put(V k, Budget b) {
//        //TODO use Map.compute.., etc
//
//        BagBudget<V> v = getBudget(k);
//
//        if (v!=null) {
//            v.set(b);
//            return k;
//        } else {
//            index.put(k, b);
//            return null;
//        }
//    }

    public boolean isSorted() {
        return items.isSorted();
    }

//    protected CurveMap newIndex() {
//        return new CurveMap(
//                //new HashMap(capacity)
//                Global.newHashMap(capacity()),
//                //new UnifiedMap(capacity)
//                //new CuckooMap(capacity)
//                items
//        );
//    }

    @Override
    public final void clear() {
        items.clear();
        index.clear();
    }

    @Override
    public BLink<V> sample() {
        throw new RuntimeException("unimpl");
    }

    @Override
    public ArrayBag<V> sample(int n, Predicate<BLink> each, Collection<BLink<V>> target) {
        throw new RuntimeException("unimpl");
    }

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    @Override
    public final int size() {
        /*if (Global.DEBUG)
            validate();*/
        return items.size();
    }

    public void validate() {
        int in = index.size();
        int is = items.size();
        if (Math.abs(is - in) > 0) {
//                System.err.println("INDEX");
//                for (Object o : index.values()) {
//                    System.err.println(o);
//                }
//                System.err.println("ITEMS:");
//                for (Object o : items) {
//                    System.err.println(o);
//                }

            Set<V> difference = Sets.symmetricDifference(
                    new HashSet(index.values()),
                    new HashSet(items)
            );

            System.err.println("DIFFERENCE");
            for (Object o : difference) {
                System.err.println("  " + o);
            }

            throw new RuntimeException("curvebag fault: " + in + " index, " + is + " items");
        }

//            //test for a discrepency of +1/-1 difference between name and items
//            if ((is - in > 2) || (is - in < -2)) {
//                System.err.println(this.getClass() + " inconsistent index: items=" + is + " names=" + in);
//                /*System.out.println(nameTable);
//                System.out.println(items);
//                if (is > in) {
//                    List<E> e = new ArrayList(items);
//                    for (E f : nameTable.values())
//                        e.remove(f);
//                    System.out.println("difference: " + e);
//                }*/
//                throw new RuntimeException(this.getClass() + " inconsistent index: items=" + is + " names=" + in);
//            }
    }

    @Override
    public final void setCapacity(int capacity) {
        items.setCapacity(capacity);
    }

    /**
     * Check if an item is in the bag
     *
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    @Override
    public boolean contains(V it) {
        return index.containsKey(it);
    }

//    /**
//     * Get an Item by key
//     *
//     * @param key The key of the Item
//     * @return The Item with the given key
//     */
//    @Override
//    public V get(K key) {
//        //TODO combine into one Map operation
//        V v = index.get(key);
//        if (v!=null && v.getBudget().isDeleted()) {
//            index.remove(key);
//            return null;
//        }
//        return v;
//    }

    @Override
    public BLink<V> remove(V key) {
        return index.remove(key);
    }


    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    @Override
    public BLink<V> pop() {
        throw new RuntimeException("unimpl");
    }



    /**
     * Insert an item into the itemTable
     *
     * @param i The Item to put in
     * @return The updated budget
     */
    @Override
    public final BLink<V> put(Object i, Budget b, float scale) {

        ArrayMapping index = this.index;

        BLink<V> existing = index.get(i);

        if (existing != null) {

            if (existing!=b)
                merge(existing, b, scale);

            update(existing);

            return existing;

        } else {

            BLink newBudget;
            if (!(b instanceof BLink)) {
                newBudget = new BLink(i, b, scale);
            } else {
                //use provided
                newBudget = (BLink)b;
                newBudget.commit();
            }

            BLink<V> displaced = index.put((V) i, newBudget);
            if (displaced!=null) {
                if (displaced == newBudget)
                    return null; //wasnt inserted
                else {
                    //remove what was been removed from the items list
                    index.removeKey(displaced.get());
                }
            }

            return newBudget;

        }

//        //TODO optional displacement until next update, allowing sub-threshold to grow beyond threshold
//        BagBudget<V> displaced = null;
//        if (full()) {
//            if (getPriorityMin() > b.getPriority()) {
//                //insufficient priority to enter the bag
//                //remove the key which was put() at beginning of this method
//                return index.removeKey(i);
//            }
//            displaced = removeLowest();
//        }
        //now that room is available:

    }




    @Override
    public final void commit() {
        top(this::update);
    }

    public void update(BLink<V> v) {
        if (!v.hasDelta()) {
            return;
        }
        if (size() == 1) {
            v.commit();
            return;
        }

        SortedIndex<BLink<V>> ii = this.items;

        int currentIndex = ii.locate(v);
        if (currentIndex == -1) {
            //an update for an item which has been removed already. must be re-inserted
            v.commit();
            put(v);
            return;
        }

        v.commit();

        float newScore = ii.score(v);
        if ((newScore < ii.scoreAt(currentIndex+1)) || //score of item below
                (newScore > ii.scoreAt(currentIndex-1)) //score of item above
            ) {
            ii.remove(currentIndex);
            ii.insert(v); //reinsert
        } else {
            //otherwise, it remains in the same position and move unnecessary
        }
    }

    /**
     * TODO make this work for the original condition: (size() >= capacity)
     * all comparisons like this should use this same condition
     */
    final boolean isFull() {
        return (size() >= capacity());
    }

    protected final BLink<V> removeLowest() {
        if (isEmpty()) return null;
        return removeItem(size() - 1);
    }

    protected final BLink<V> removeHighest() {
        if (isEmpty()) return null;
        return removeItem(0);
    }

    public final BLink<V> highest() {
        if (isEmpty()) return null;
        return getItem(0);
    }

    public final BLink<V> lowest() {
        if (isEmpty()) return null;
        return getItem(size() - 1);
    }

    final BLink<V> getItem(int index) {
        return items.get(index);
    }

    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @return The first Item
     */
    final BLink<V> removeItem(int index) {

        BLink<V> ii = getItem(index);
        /*if (ii == null)
            throw new RuntimeException("invalid index: " + index + ", size=" + size());*/

        //        if (ii!=jj) {
//            throw new RuntimeException("removal fault");
//        }

        return remove(ii.get());
    }

    @Override
    public BLink<V> get(Object key) {
        return index.get(key);
    }

    @Override
    public final int capacity() {
        return items.capacity();
    }

    @Override
    public String toString() {
        return super.toString() + '{' + items.getClass().getSimpleName() + '}';
    }

    @Override
    public final Collection<V> values() {
        return index.keySet();
    }

    @Override
    public final Iterator<V> iterator() {
        return Iterators.transform(items.iterator(), t-> t.get() );
    }

    @Override
    public final void forEach(Consumer<? super V> action) {

        //items.forEach(b -> action.accept(b.get()));

        final List<BLink<V>> l = items.getList();

        //start at end
        int n = l.size();
        for (int i = 0; i < n; i++) {
        //for (int i = l.size()-1; i >= 0; i--){
            action.accept(l.get(i).get());
        }

    }

    /**
     * default implementation; more optimal implementations will avoid instancing an iterator
     */
    @Override
    public void forEach(int max, Consumer<? super V> action) {
        List<BLink<V>> l = items.getList();
        int n = Math.min(l.size(), max);
        //TODO let the list implementation decide this because it can use the array directly in ArraySortedIndex
        for (int i = 0; i < n; i++) {
            action.accept(l.get(i).get());
        }
    }

    @Override public void topWhile(Predicate<BLink> action) {
        List<BLink<V>> l = items.getList();
        int n = l.size();
        for (int i = 0; i < n; i++) {
            if (!action.test(l.get(i)))
                break;
        }
    }

    @Override public void top(Consumer<BLink> action) {
        items.getList().forEach(action);
    }
    @Override public void topN(int limit, Consumer<BLink> action) {
        List<BLink<V>> l = items.getList();
        int n = Math.min(l.size(), limit);
        for (int i = 0; i < n; i++)
            action.accept(l.get(i));
    }

    @Override
    public float getPriorityMax() {
        if (isEmpty()) return 0;
        return items.getFirst().getPriority();
    }

    @Override
    public float getPriorityMin() {
        if (isEmpty()) return 0;
        return items.getLast().getPriority();
    }

    public final void popAll(Consumer<? super V> receiver) {
        forEach(receiver);
        clear();
    }

    public void pop(Consumer<? super V> receiver, int n) {
        if (n == size()) {
            //special case where size <= inputPerCycle, the entire bag can be flushed in one operation
            popAll(receiver);
        } else {
            for (int i = 0; i < n; i++) {
                receiver.accept(pop().get());
            }
        }
    }


    public class ArrayMapping extends CollectorMap<V, BLink<V>> {

        final SortedIndex<BLink<V>> items;

        public ArrayMapping(Map<V, BLink<V>> map, SortedIndex<BLink<V>> items) {
            super(map);
            this.items = items;
        }

        @Override
        public final BudgetMerge getMerge() {
            return mergeFunction;
        }

        @Override
        protected BLink<V> removeItem(BLink<V> removed) {

            if (items.remove(removed)) {
                return removed;
            }

            return null;
        }

        @Override
        protected BLink<V> addItem(BLink<V> i) {
            BLink<V> overflow = items.insert(i);
            if (overflow!=null) {
                removeKey(overflow.get());
            }
            return overflow;
        }
    }
}
