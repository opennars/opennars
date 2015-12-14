package nars.bag.impl;

import com.google.common.collect.Sets;
import com.gs.collections.api.block.procedure.Procedure2;
import nars.Global;
import nars.bag.Bag;
import nars.bag.BagBudget;
import nars.budget.Budget;
import nars.budget.UnitBudget;
import nars.util.CollectorMap;
import nars.util.data.sorted.SortedIndex;

import java.util.*;
import java.util.function.Consumer;

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
    public final SortedIndex<BagBudget<V>> items;


    public ArrayBag(SortedIndex<BagBudget<V>> items) {
        this(items, Global.newHashMap(items.capacity()));
    }

    public ArrayBag(SortedIndex<BagBudget<V>> items, Map<V, BagBudget<V>> map) {

        items.clear();

        this.items = items;
        index = new ArrayMapping(map, items);
    }

    @Override public BagBudget<V> put(V k) {
        return put(k, UnitBudget.zero);
    }
    @Override public BagBudget<V> put(V k, BagBudget<V> budget) {
        throw new RuntimeException("unimpl?");
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
    public V peekNext() {
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
    public BagBudget<V> remove(V key) {
        return index.remove(key);
    }



    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    @Override
    public V pop() {
        throw new RuntimeException("unimpl");
    }



    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param i The Item to put in
     * @return The overflow Item, or null if nothing displaced
     */
    @Override
    public BagBudget<V> put(V i, Budget b) {


        ArrayMapping index = this.index;

        BagBudget<V> existing = index.get(i);

        if (existing != null) {

            index.removeItem(existing);

            if (!existing.isDeleted()) {

                /*if (index.removeItem(overflow) == null)
                    throw new RuntimeException("put fault");*/

                merge(existing.getBudget(), b);

                index.addItem(existing);

                /*if (!i.name().equals(overflow.name())) {
                    throw new RuntimeException("wtf: notEqual " + i.name() + " and " + overflow.name() );
                }*/

                /* absorbed */
                return null;
            }
        }


        BagBudget<V> displaced = null;

        if (full()) {


            if (getPriorityMin() > b.getPriority()) {
                //insufficient priority to enter the bag
                //remove the key which was put() at beginning of this method
                return index.removeKey(i);
            }

            displaced = removeLowest();
        }

        //now that room is available:
        index.addItem(new BagBudget(i, b));

        return displaced;
    }

    /**
     * TODO make this work for the original condition: (size() >= capacity)
     * all comparisons like this should use this same condition
     */
    final boolean full() {
        return (size() >= capacity());
    }

    protected final BagBudget<V> removeLowest() {
        if (isEmpty()) return null;
        return removeItem(size() - 1);
    }

    protected final BagBudget<V> removeHighest() {
        if (isEmpty()) return null;
        return removeItem(0);
    }

    public final BagBudget<V> highest() {
        if (isEmpty()) return null;
        return getItem(0);
    }

    public final BagBudget<V> lowest() {
        if (isEmpty()) return null;
        return getItem(size() - 1);
    }

    final BagBudget<V> getItem(int index) {
        return items.get(index);
    }

    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @return The first Item
     */
    final BagBudget<V> removeItem(int index) {

        BagBudget<V> ii = getItem(index);
        /*if (ii == null)
            throw new RuntimeException("invalid index: " + index + ", size=" + size());*/

        //        if (ii!=jj) {
//            throw new RuntimeException("removal fault");
//        }

        return remove(ii.get());
    }

    @Override
    public BagBudget<V> get(V key) {
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
        return index.keySet().iterator();
    }

    @Override
    public final void forEach(Consumer<? super V> action) {

        index.keySet().forEach(action);
//
//        final List<V> l = items.getList();
//
//        //start at end
//        for (int i = l.size()-1; i >= 0; i--){
//            action.accept(l.get(i));
//        }
//
    }

    /**
     * default implementation; more optimal implementations will avoid instancing an iterator
     */
    @Override
    public void forEach(int max, Consumer<? super V> action) {
        List<BagBudget<V>> l = items.getList();
        int n = Math.min(l.size(), max);
        //TODO let the list implementation decide this because it can use the array directly in ArraySortedIndex
        for (int i = 0; i < n; i++) {
            action.accept(l.get(i).get());
        }
    }

    @Override
    public void forEachEntry(Consumer<BagBudget> action) {
        List<BagBudget<V>> l = items.getList();
        int n = l.size();
        for (int i = 0; i < n; i++) {
            action.accept(l.get(i));
        }
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
                receiver.accept(pop());
            }
        }
    }


    public class ArrayMapping extends CollectorMap<V, BagBudget<V>> {

        final SortedIndex<BagBudget<V>> items;

        public ArrayMapping(Map<V, BagBudget<V>> map, SortedIndex<BagBudget<V>> items) {
            super(map);
            this.items = items;
        }

        @Override
        public final Procedure2<Budget, Budget> getMerge() {
            return mergeFunction;
        }

        @Override
        protected BagBudget<V> removeItem(BagBudget<V> removed) {

            if (items.remove(removed)) {
                return removed;
            }

            return null;
        }

        @Override
        protected BagBudget<V> addItem(BagBudget<V> i) {

            return items.insert(i);
        }
    }
}
