package nars.bag.impl;

import com.google.common.collect.Sets;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import nars.bag.Bag;
import nars.budget.Itemized;
import nars.nal.UtilityFunctions;
import nars.util.CollectorMap;
import nars.util.data.sorted.SortedIndex;
import nars.util.sort.ArraySortedIndex;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Bag which stores items, sorted, in one array.
 * Removal policy can select items by percentile via the array index.
 * A curve function maps a probabilty distribution to an index allowing the bag
 * to choose items with certain probabilities more than others.
 * <p>
 * In theory, the curve can be calculated to emulate any potential removal policy.
 * <p>
 * Insertion into the array is a O(log(n)) insertion sort, plus O(N) to shift items (unless the array is tree-like and can avoid this cost).
 * Removal is O(N) to shift items, and an additional possible O(N) if a specific item to be removed is not found at the index expected for its current priority value.
 * <p>
 * TODO make a CurveSampling interface with at least 2 implementations: Random and LinearScanning. it will use this instead of the 'boolean random' constructor argument
 */
public class CurveBag<K, V extends Itemized<K>> extends Bag<K, V> {

    /**
     * mapping from key to item
     */
    public final CurveMap index;

    /**
     * array of lists of items, for items on different level
     */
    public final SortedIndex<V> items;

    /**
     * defined in different bags
     */
    final int capacity;


    public final CurveSampler sampler;


    public static <E extends Itemized> SortedIndex<E> defaultIndex(int capacity) {
        //if (capacity < 50)            
        return new ArraySortedIndex(capacity);
        //else
        //    return new FractalSortedItemList<E>();
    }

    public CurveBag(Random rng, int capacity) {
        this(rng, capacity, new Power6BagCurve());
    }


    public CurveBag(Random rng, int capacity, BagCurve curve, SortedIndex<V> ind) {
        this(capacity, new RandomSampler(rng, curve), ind);
    }

    public CurveBag(Random rng, int capacity, BagCurve curve) {
        this(capacity, new RandomSampler(rng, curve), defaultIndex(capacity));
                
                                /*if (capacity < 128)*/
                //items = new ArraySortedItemList<>(capacity);
                /*else  {
                    //items = new FractalSortedItemList<>(capacity);
                    //items = new RedBlackSortedItemList<>(capacity);
                }*/

    }

    class CurveMap extends CollectorMap<K, V> {

        public CurveMap(Map<K, V> map) {
            super(map);
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

            final V e = items.insert(i);

            return e;
        }
    }

    public boolean isSorted() {
        return items.isSorted();
    }

    @FunctionalInterface
    public interface CurveSampler {
        /** which index to select */
        public int next(CurveBag b);
    }

    public static class RandomSampler implements CurveSampler {

        private final BagCurve curve;
        private final Random rng;

        public RandomSampler(Random rng, BagCurve curve) {
            this.curve = curve;
            this.rng = rng;
        }

        /** maps y in 0..1.0 to an index in 0..size */
        final int index(final float y, final int size) {

            if (y < 0) return 0;

            /** using 1-y because the list of items is stored top priority first */
            int i= UtilityFunctions.floorInt((1-y) * size);

            if (i >= size) return size-1;
            if (i < 0) return 0;

            return i;

            /*if (result == size) {
                //throw new RuntimeException("Invalid removal index: " + x + " -> " + y + " " + result);
                return (size - 1);
            }*/

            //return result;

        }

        @Override
        public int next(final CurveBag b) {
            final int s = b.size();
            if (s == 1) return 0;

            float x = rng.nextFloat();

            final float min = b.getPriorityMin();
            final float max = b.getPriorityMax();
            if (min!=max) {
                //rescale to dynamic range
                x = min + (x * (max-min));
            }

            float y = curve.y(x);

            if (min!=max) {
                final float yMin = curve.y(min);
                final float yMax = curve.y(max);
                y = (y - yMin) / (yMax - yMin);
            }

            return index(y, s);
        }
    }

//FOR linear scanner, if re-implemented
//    /**
//     * Rate of sampling index when in non-random "scanning" removal mode.
//     * The position will be incremented/decremented by scanningRate/(numItems+1) per removal.
//     * Default scanning behavior is to start at 1.0 (highest priority) and decrement.
//     * When a value exceeds 0.0 or 1.0 it wraps to the opposite end (modulo).
//     * <p>
//     * Valid values are: -1.0 <= x <= 1.0, x!=0
//     */
//    final float scanningRate = -1.0f;


    public CurveBag(int capacity, CurveSampler sampler, SortedIndex<V> items) {
        super();
        this.capacity = capacity;
        this.sampler = sampler;


        items.clear();
        items.setCapacity(capacity);
        this.items = items;


        index = new CurveMap(
                //new HashMap(capacity)
                //Global.newHashMap(capacity)
                new UnifiedMap(capacity)
                //new CuckooMap(capacity)
        );

    }


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
    public int size() {
        return items.size();
    }

    void validate() {
        int in = index.size();
        int is = items.size();
            if (Math.abs(is-in) > 2) {
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
        return index.get(key);
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
    public V pop() {

        if (isEmpty()) return null; // empty bag
        return removeItem(sampler.next(this));

    }


    @Override
    public V peekNext() {

        if (isEmpty()) return null; // empty bag
        return items.get(sampler.next(this));

    }





//    public static long fastRound(final double d) {
//        if (d > 0) {
//            return (long) (d + 0.5d);
//        } else {
//            return (long) (d - 0.5d);
//        }
//    }
//    


    @Override
    public float getPriorityMin() {
        if (isEmpty()) return 0;
        return items.getLast().getPriority();
    }

    @Override
    public float getPriorityMax() {
        if (isEmpty()) return 0;
        return items.getFirst().getPriority();
    }


    //    /**
//     * calls overflow() on an overflown object
//     * returns the updated or created concept (not overflow like PUT does (which follows Map.put() semantics)
//     * NOTE: this is the generic version which may or may not work, or be entirely efficient in some subclasses
//     */
//    public V update(final BagTransaction<K, V> selector) {
//
//
//        if (Global.DEBUG && !isSorted()) {
//            throw new RuntimeException("not sorted");
//        }
//
//        K key = selector.name();
//        V item;
//        if (key != null) {
//            item = get(key);
//        }
//        else {
//            item = peekNext();
//        }
//
//        if (item == null) {
//            item = selector.newItem();
//            if (item == null)
//                return null;
//            else {
//                // put the (new or merged) item into itemTable
//                final V overflow = put(item);
//
//                if (overflow != null)
//                    selector.overflow(overflow);
//                else if (overflow == item)
//                    return null;
//
//
//                return item;
//            }
//        } else {
//
//
//            remove(item.name());
//
//            final V changed = selector.update(item);
//
//
//            if (changed == null) {
//
//                put(item);
//
//                return item;
//            }
//            else {
//                //it has changed
//
//
//                final V overflow = put(changed);
//
//                /*if (overflow == changed)
//                    return null;*/
//
//                if (overflow != null) // && !overflow.name().equals(changed.name()))
//                    selector.overflow(overflow);
//
//                return changed;
//            }
//        }
//
//
//    }

    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param i The Item to put in
     * @return The overflow Item, or null if nothing displaced
     */
    @Override
    public V put(final V i) {

        boolean full = (size() >= capacity);

        V overflow = index.remove(i.name());

        if (overflow!=null) {
            if (overflow!=i)
                merge(i.getBudget(), overflow.getBudget());
            full = false;
        }

        if (full) {

            if (getPriorityMin() > i.getPriority()) {
                //insufficient priority to enter the bag
                return i;
            }

            overflow = removeLowest();
        }

        index.put(i);

        return overflow;

    }


    protected V removeLowest() {
        return removeItem(0);
    }

    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    protected V removeItem(final int index) {

        V ii = items.get(index);
        if (ii == null)
            return null;

        return remove( ii.name() );

    }



    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return super.toString() + '{' + items.getClass().getSimpleName() + '}';
    }

    @Override
    public Set<K> keySet() {
        return index.keySet();
    }

    @Override
    public Collection<V> values() {
        return index.values();
    }

    @Override
    public Iterator<V> iterator() {
        return items.descendingIterator();
    }

    /**
     * Defines the focus curve.  x is a proportion between 0 and 1 (inclusive).
     * x=0 represents low priority (bottom of bag), x=1.0 represents high priority
     *
     * @param x input mappig value
     * @return
     */
    public static interface BagCurve extends Serializable {

        public float y(float x);
    }


    public static class CubicBagCurve implements BagCurve {

        @Override
        public final float y(final float x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-       
            float nx = 1.0f - x;
            return 1.0f - (nx * nx * nx);
        }

        @Override
        public String toString() {
            return "CubicBagCurve";
        }
    }


    public static class Power4BagCurve implements BagCurve {

        @Override
        public final float y(final float x) {
            float nx = 1.0f - x;
            float nnx = nx * nx;
            return 1.0f - (nnx * nnx);
        }

        @Override
        public String toString() {
            return "Power4BagCurve";
        }
    }

    public static class Power6BagCurve implements BagCurve {

        @Override
        public final float y(final float x) {
            float nx = 1.0f - x;
            float nnx = nx * nx;
            return 1.0f - (nnx * nnx * nnx);
        }

        @Override
        public String toString() {
            return "Power6BagCurve";
        }
    }

    /**
     * Approximates priority -> probability fairness with an exponential curve
     */
    @Deprecated
    public static class FairPriorityProbabilityCurve implements BagCurve {

        @Override
        public final float y(final float x) {
            return (float) (1f - Math.exp(-5f * x));
        }

        @Override
        public String toString() {
            return "FairPriorityProbabilityCurve";
        }

    }


    public static class QuadraticBagCurve implements BagCurve {

        @Override
        public final float y(final float x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-       
            float nx = 1f - x;
            return 1f - (nx * nx);
        }

        @Override
        public String toString() {
            return "QuadraticBagCurve";
        }

    }

    @Override
    public void forEach(final Consumer<? super V> action) {

        final List<V> l = items.getList();
        for (int i = 0; i < l.size(); i++){
            action.accept(l.get(i));
        }

    }


    /** default implementation; more optimal implementations will avoid instancing an iterator */
    public void forEach(final int max, final Consumer<V> action) {
        final List<V> l = items.getList();
        final int n = Math.min(l.size(), max);
        //TODO let the list implementation decide this because it can use the array directly in ArraySortedIndex
        for (int i = 0; i < n; i++){
            action.accept(l.get(i));
        }
    }

}
