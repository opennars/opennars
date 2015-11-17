package nars.bag.impl;

import com.google.common.collect.Sets;
import com.gs.collections.api.block.procedure.Procedure2;
import nars.Global;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.budget.Itemized;
import nars.util.CollectorMap;
import nars.util.data.sorted.SortedIndex;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.function.Consumer;

/**
 * A bag implemented as a combination of a Map and a SortedArrayList
 */
public abstract class ArrayBag<K, V extends Itemized<K>> extends Bag<K, V> implements Iterable<V> {

    public final static Procedure2<Budget, Budget> DEFAULT_MERGE_METHOD = Budget.average;

    /**
     * mapping from key to item
     */
    public final ArrayMapping<K,V> index;

    /**
     * array of lists of items, for items on different level
     */
    public final SortedIndex<V> items;

    public ArrayBag(SortedIndex<V> items) {
        this(items, Global.newHashMap(items.capacity()));
    }

    public ArrayBag(SortedIndex<V> items, Map<K,V> map) {
        super();

        items.clear();

        this.items = items;
        this.index = new ArrayMapping(map, items);
    }

//    public static <E extends Itemized> SortedIndex<E> defaultIndex(int capacity) {
//        //if (capacity < 50)
//        return new ArraySortedIndex(capacity);
//        //else
//        //    return new FractalSortedItemList<E>();
//    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(capacity());
        super.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setCapacity( in.readInt() );
        super.readExternal(in);
    }

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

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    @Override
    final public int size() {
        /*if (Global.DEBUG)
            validate();*/
        return items.size();
    }

    public void validate() {
        int in = index.size();
        int is = items.size();
            if (Math.abs(is-in) > 0) {
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
    public boolean contains(final V it) {
        return index.containsValue(it);
    }

    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    @Override
    public V get(final K key) {
        //TODO combine into one Map operation
        V v = index.get(key);
        if (v!=null && v.getBudget().isDeleted()) {
            index.remove(key);
            return null;
        }
        return v;
    }

    @Override
    public V remove(final K key) {
        return index.remove(key);
    }

    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    @Override
    final public V pop() {
        return peekNext(true);
    }

    abstract public V peekNext(final boolean remove);

    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param i The Item to put in
     * @return The overflow Item, or null if nothing displaced
     */
    @Override
    public V put(final V i) {


        final ArrayMapping<K, V> index = this.index;

        final V overflow = index.putKey(i.name(), i);


        if (overflow!=null) {

            index.removeItem(overflow);

            if (!overflow.isDeleted()) {

                /*if (index.removeItem(overflow) == null)
                    throw new RuntimeException("put fault");*/

                merge(i.getBudget(), overflow.getBudget());

                index.addItem(i);

                /*if (!i.name().equals(overflow.name())) {
                    throw new RuntimeException("wtf: notEqual " + i.name() + " and " + overflow.name() );
                }*/

                /* absorbed */
                return null;
            }
        }


            V displaced = null;

            if (full()) {


                if (getPriorityMin() > i.getPriority()) {
                    //insufficient priority to enter the bag
                    //remove the key which was put() at beginning of this method
                    index.removeKey(i.name());
                    return i;
                }

                displaced = removeLowest();
            }

            //now that room is available:
            index.addItem(i);

            return displaced;
    }

    /** TODO make this work for the original condition: (size() >= capacity)
     * all comparisons like this should use this same condition
     * */
    final boolean full() {
        return (size() >= capacity());
    }

    final protected V removeLowest() {
        if (isEmpty()) return null;
        return removeItem(size()-1);
    }
    final protected V removeHighest() {
        if (isEmpty()) return null;
        return removeItem(0);
    }

    final public V highest() {
        if (isEmpty()) return null;
        return getItem(0);
    }
    final public V lowest() {
        if (isEmpty()) return null;
        return getItem(size()-1);
    }

    final V getItem(final int index) {
        return items.get(index);
    }

    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @return The first Item
     */
    final V removeItem(final int index) {

        final V ii = getItem(index);
        /*if (ii == null)
            throw new RuntimeException("invalid index: " + index + ", size=" + size());*/

        //        if (ii!=jj) {
//            throw new RuntimeException("removal fault");
//        }

        return remove( ii.name() );
    }

    @Override
    final public int capacity() {
        return items.capacity();
    }

    @Override
    public String toString() {
        return super.toString() + '{' + items.getClass().getSimpleName() + '}';
    }

    @Override
    final public Set<K> keySet() {
        return index.keySet();
    }

    @Override
    final public Collection<V> values() {
        return items; //index.values();
    }

    @Override
    final public Iterator<V> iterator() {
        return items.iterator();
    }

    @Override
    public final void forEach(final Consumer<? super V> action) {

        items.forEach(action);
//
//        final List<V> l = items.getList();
//
//        //start at end
//        for (int i = l.size()-1; i >= 0; i--){
//            action.accept(l.get(i));
//        }
//
    }

    /** default implementation; more optimal implementations will avoid instancing an iterator */
    @Override
    public void forEach(final int max, final Consumer<V> action) {
        final List<V> l = items.getList();
        final int n = Math.min(l.size(), max);
        //TODO let the list implementation decide this because it can use the array directly in ArraySortedIndex
        for (int i = 0; i < n; i++){
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
        }
        else {
            for (int i = 0; i < n; i++) {
                receiver.accept(pop());
            }
        }
    }


    public class ArrayMapping<K, V extends Itemized<K>> extends CollectorMap<K, V> {

        final SortedIndex<V> items;

        public ArrayMapping(Map<K, V> map, SortedIndex<V> items) {
            super(map);
            this.items = items;
        }

        @Override
        public final Procedure2<Budget, Budget> getMerge() {
            return mergeFunction;
        }

        @Override
        protected V removeItem(final V removed) {

            if (items.remove(removed)) {
                return removed;
            }

            return null;
        }

        @Override
        protected V addItem(final V i) {

            return items.insert(i);
        }
    }
}
