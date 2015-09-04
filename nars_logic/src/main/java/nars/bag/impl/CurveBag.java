package nars.bag.impl;

import com.google.common.collect.Sets;
import com.gs.collections.api.block.function.primitive.FloatToFloatFunction;
import nars.Global;
import nars.bag.Bag;
import nars.bag.BagSelector;
import nars.budget.Itemized;
import nars.nal.UtilityFunctions;
import nars.util.CollectorMap;
import nars.util.data.sorted.SortedIndex;
import nars.util.sort.ArraySortedIndex;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

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
        this(rng, capacity, CurveBag.power6BagCurve);
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



    public class CurveMap extends CollectorMap<K, V> {

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
    public interface CurveSampler extends ToIntFunction<CurveBag> {    }

    public static class RandomSampler implements CurveSampler {

        private final BagCurve curve;
        private final Random rng;

        public RandomSampler(Random rng, BagCurve curve) {
            this.curve = curve;
            this.rng = rng;
        }

        /** maps y in 0..1.0 to an index in 0..size */
        final int index(float y, final int size) {

            //y = 1f - y; //reverse for the ordering of the bag

            if (y <= 0) return 0;

            int i= UtilityFunctions.floorInt(y * size);

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
        public int applyAsInt(final CurveBag b) {
            final int s = b.size();
            if (s == 1) return 0;

            float x = rng.nextFloat();

            //TODO cache these curvepoints when min/max dont change
            final float min = b.getPriorityMin();
            final float max = b.getPriorityMax();
            if (min!=max) {
                //rescale to dynamic range
                x = min + (x * (max-min));
            }

            float y = curve.valueOf(x);

            if (min!=max) {
                final float yMin = curve.valueOf(min);
                final float yMax = curve.valueOf(max);
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


        index = newIndex(capacity);

    }

    protected CurveMap newIndex(int capacity) {
        return new CurveMap(
                //new HashMap(capacity)
                Global.newHashMap(capacity)
                //new UnifiedMap(capacity)
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
    final public V pop() {
        return peekNext(true);
    }

    @Override
    final public V peekNext() {
        return peekNext(false);
    }

    public V peekNext(final boolean remove) {

        while (!isEmpty()) {

            final int index = sampler.applyAsInt(this);

            final V i =
                    remove ? removeItem(index): items.get(index);

            if (i == null)
                return null;

            if (!i.getBudget().isDeleted()) {
                return i;
            }

            //ignore this deleted item now that it's removed from the bag
            //if it wasnt already removed above
            if (!remove)
                remove(i.name());

            continue;

        }
        return null; // empty bag
    }





//    public static long fastRound(final double d) {
//        if (d > 0) {
//            return (long) (d + 0.5d);
//        } else {
//            return (long) (d - 0.5d);
//        }
//    }
//    




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


        final CurveMap index = this.index;

        final V overflow = index.putKey(i.name(), i);

        if (overflow!=null) {

            index.removeItem(overflow);

            /*if (index.removeItem(overflow) == null)
                throw new RuntimeException("put fault");*/

            merge(i.getBudget(), overflow.getBudget());

            index.addItem(i);

            if (!i.name().equals(overflow.name())) {
                throw new RuntimeException("wtf");
            }

            /* absorbed */
            return null;
        }

        else {

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
    }


    /** TODO make this work for the original condition: (size() >= capacity)
     * all comparisons like this should use this same condition
     * */
    final boolean full() {
        return (size() >= capacity);
    }


    final protected V removeLowest() {
        return removeItem(size()-1);
    }

    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    protected final V removeItem(final int index) {

        final V ii = items.get(index);
        if (ii == null)
            throw new RuntimeException("invalid index: " + index + ", size=" + size());

        final V jj = remove( ii.name() );
        /*if (ii!=jj) {
            throw new RuntimeException("removal fault");
        }*/

        return jj;
    }



    @Override
    final public int capacity() {
        return capacity;
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
        return index.values();
    }

    @Override
    final public Iterator<V> iterator() {
        return items.iterator();
    }

    /**
     * Defines the focus curve.  x is a proportion between 0 and 1 (inclusive).
     * x=0 represents low priority (bottom of bag), x=1.0 represents high priority
     *
     * @param x input mappig value
     * @return
     */

    @FunctionalInterface
    public static interface BagCurve extends FloatToFloatFunction {
        //public float valueOf(float x);
    }


    public static class CubicBagCurve implements BagCurve {

        @Override
        public final float valueOf(final float x) {
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
        public final float valueOf(final float x) {
            float nx = 1-x;
            float nnx = nx * nx;
            return 1 - (nnx * nnx);
        }

        @Override
        public String toString() {
            return "Power4BagCurve";
        }
    }
    public final static BagCurve power4BagCurve = new Power4BagCurve();

    public static class Power6BagCurve implements BagCurve {

        @Override
        public final float valueOf(final float x) {
            /** x=0, y=0 ... x=1, y=1 */
            float nx = 1-x;
            float nnx = nx * nx;
            return 1-(nnx * nnx * nnx);
        }

        @Override
        public String toString() {
            return "Power6BagCurve";
        }
    }

    public final static BagCurve power6BagCurve = new Power6BagCurve();


    /**
     * Approximates priority -> probability fairness with an exponential curve
     */
    @Deprecated
    public static class FairPriorityProbabilityCurve implements BagCurve {

        @Override
        public final float valueOf(final float x) {
            return (float) (1f - Math.exp(-5f * x));
        }

        @Override
        public String toString() {
            return "FairPriorityProbabilityCurve";
        }

    }


    public static class QuadraticBagCurve implements BagCurve {

        @Override
        public final float valueOf(final float x) {
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

    /** optimized peek implementation that scans the curvebag
     *  iteratively surrounding a randomly selected point */
    @Override protected int peekFill(BagSelector<K, V> tx, V[] batch, int start, int len, int maxAttempts) {



        final Function<V, BagSelector.ForgetAction> filter = tx.getModel();

        final int s = size();

        final List<V> a = items.getList();

        int fill;

        if (len == s) {
            //optimization: if len==s then just add all elements
            fill = 0;
            for (int i = 0; i < len; i++) {
                V v = a.get(i);
                BagSelector.ForgetAction p = filter.apply(v);
                if ((p!= BagSelector.ForgetAction.Ignore) && (p!= BagSelector.ForgetAction.IgnoreAndForget))
                    batch[start + (fill++)] = v;

            }
            return fill;
        }

        final int r = len/2; //radius padding

        int center = this.sampler.applyAsInt(this);
        if (center + r >= s)
            center = s - r;
        if (center -r < 0)
            center = r;


        batch[0] = a.get(center); //should not be null
        fill = 1;

        int remaining = maxAttempts;

        boolean direction = true;

        int nextUp = center, nextDown = center;
        boolean finishedUp = false, finishedDown = false;

        while( (remaining > 0 ) && (fill < len) ) {

            if (++nextUp<s) {
                final V x = a.get(nextUp);
                if (!bufferIncludes(batch, x)) {
                    BagSelector.ForgetAction p = filter.apply(x);
                    if ((p!= BagSelector.ForgetAction.Ignore) && (p!= BagSelector.ForgetAction.IgnoreAndForget))
                        batch[start + (fill++)] = x;
                }
            }
            else {
                if (finishedDown) break;
                finishedUp = true;
            }

            if (fill == len) break;

            if (--nextDown >=0) {
                final V x = a.get(nextDown);

                if (!bufferIncludes(batch, x)) {
                    BagSelector.ForgetAction p = filter.apply(x);
                    if ((p!= BagSelector.ForgetAction.Ignore) && (p!= BagSelector.ForgetAction.IgnoreAndForget))
                        batch[start + (fill++)] = x;
                }
            }
            else {
                if (finishedUp) break;
                finishedDown=true;
            }

            remaining-=2;

        }

        /*
        System.out.println(Arrays.toString(batch) + " " + nextDown + ":" + center + ":" + nextUp +
                " --> " + fill + '/' + len + '/' + s + " found, " + (maxAttempts-remaining) + " tried");
            */

        return fill;
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

    //    @Override
//    protected int update(BagTransaction<K, V> tx, V[] batch, int start, int stop, int maxAdditionalAttempts) {
//
//        super.update()
//        int center = this.sampler.applyAsInt(this);
//
//    }
}
