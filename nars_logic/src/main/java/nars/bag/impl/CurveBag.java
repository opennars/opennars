package nars.bag.impl;

import com.gs.collections.api.block.function.primitive.FloatToFloatFunction;
import nars.bag.BLink;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.budget.BudgetMerge;
import nars.util.ArraySortedIndex;
import nars.util.data.sorted.SortedIndex;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
public class CurveBag<V> extends Bag<V> {

    final ArrayBag<V> arrayBag;

    public static final BagCurve power2BagCurve = new Power2BagCurve();
    public static final BagCurve power4BagCurve = new Power4BagCurve();
    public static final BagCurve power6BagCurve = new Power6BagCurve();

    //TODO move sampler features to subclass of CurveBag which specifically provides sampling
    public final BagCurve curve;
    private final Random random;

    public CurveBag(int capacity, Random rng) {
        this(
            //CurveBag.power6BagCurve,
            power4BagCurve,
            capacity, rng);
    }


    public CurveBag(BagCurve curve, int capacity, Random rng) {
        this(new ArraySortedIndex(capacity/4, capacity), curve, rng);

                                /*if (capacity < 128)*/
        //items = new ArraySortedItemList<>(capacity);
                /*else  {
                    //items = new FractalSortedItemList<>(capacity);
                    //items = new RedBlackSortedItemList<>(capacity);
                }*/

    }

    public CurveBag(SortedIndex<BLink<V>> items, BagCurve curve, Random rng) {
        super();

        this.arrayBag = new ArrayBag(items);
        this.curve = curve;
        random = rng;
    }


    @Override
    public CurveBag<V> setMergeFunction(BudgetMerge mergeFunction) {
        arrayBag.setMergeFunction(mergeFunction);
        return this;
    }

    @Override
    public BLink<V> pop() {
        return peekNext(true);
    }

    @Override
    public void commit() {
        arrayBag.commit();
    }

    public BLink<V> peekNext(boolean remove) {

        ArrayBag<V> b = this.arrayBag;

        while (!isEmpty()) {

            int index = sampleIndex();

            BLink<V> i = remove ?
                    b.removeItem(index) : b.getItem(index);

            if (!i.getBudget().getDeleted()) {
                return i;
            }

            //ignore this deleted item now that it's removed from the bag
            //if it wasnt already removed above
            if (!remove)
                remove(i.get());

        }
        return null; // empty bag
    }

    @Override
    public final void topWhile(Predicate<BLink> each) {
        arrayBag.topWhile(each);
    }

    /** optimized batch fill, using consecutive array elements, also ensuring uniqueness
     * returns the instance for fluentcy
     * */
    @Override public CurveBag<V> sample(int n, Predicate<BLink> each, Collection<BLink<V>> target) {

        int ss = size();
        final int begin, end;
        if (ss <= n) {
            //special case: give everything
            begin = 0;
            end = ss;
        } else {
            begin = Math.min(sampleIndex(), ss-n);
            end = begin + n;
        }

        for (int i = begin; i < end; i++) {
            BLink<V> ii = get(i);
            if (each == null || each.test(ii)) {
                target.add(ii);
            }
        }

        return this;
        //System.out.println("(of " + ss + ") select " + n + ": " + begin + ".." + end + " = " + target);

    }

    @Override
    public void clear() {
        arrayBag.clear();
    }

    @Override
    public BLink<V> get(Object key) {
        return arrayBag.get(key);
    }

    @Override
    public final BLink<V> sample() {
        return peekNext(false);
    }

    @Override
    public BLink<V> remove(V key) {
        return arrayBag.remove(key);
    }


    @Override
    public BLink<V> put(Object v, Budget vBagBudget, float scale) {
        return arrayBag.put(v, vBagBudget, scale);
    }


    @Override public BLink<V> put(Object v) {
        BLink<V> existing = get(v);
        return (existing != null) ?
                existing :
                put((V) v, getDefaultBudget((V) v));
    }

    protected BLink<V> getDefaultBudget(V v) {
        return new BLink(v, 0,0,0);
    }

    @Override
    public int capacity() {
        return arrayBag.capacity();
    }


    @Override
    public int size() {
        return arrayBag.size();
    }

    @Override
    public Iterator<V> iterator() {
        return arrayBag.iterator();
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

//
//    /**
//     * optimized peek implementation that scans the curvebag
//     * iteratively surrounding a randomly selected point
//     */
//    @Override
//    protected int peekNextFill(BagSelector<K, V> tx, V[] batch, int bstart, int len, int maxAttempts) {
//
//
//        int siz = size();
//        len = Math.min(siz, len);
//
//        List<V> a = arrayBag.items.getList();
//
//        int istart;
//
//        if (len != siz) {
//            //asking for some of the items
//            int r = Math.max(1, len / 2);
//            int center = sample();
//            istart = center + r; //scan downwards from here (increasing pri order)
//
//            if (r % 2 == 1) istart--; //if odd, give extra room to the start (higher priority)
//
//            //TODO test and if possible use more fair policy that accounts for clipping
//            if (istart-r < 0) {
//                istart -= (istart - r); //start further below
//            }
//            if (istart >= siz)
//                istart = siz-1;
//        }
//        else {
//            //optimization: asking for all of the items (len==siz)
//            //   just add all elements, so dont sample
//            istart = siz-1;
//        }
//
//
//        List<K> toRemove = null;
//
//        UnitBudget b = new UnitBudget(); //TODO avoid creating this
//
//
//        //int bend = bstart + len;
//        int next = bstart;
//
//        //scan increasing priority, stopping at the top or if buffer filled
//        for (int i = istart; (i >= 0) && (next < len); i--) {
//            V v = a.get(i);
//
//            if (v == null) break; //HACK wtf?
//            //throw new RuntimeException("null");
//
//            if (v.isDeleted()) {
//                if (toRemove == null) toRemove = Global.newArrayList(0); //TODO avoid creating this
//                toRemove.add(v.name());
//            } else {
//                batch[next++] = v;
//            }
//        }
//
//        //pad with nulls. helpful for garbage collection incase they contain old values (the array is meant to be re-used)
//        if (next != len)
//            Arrays.fill(batch, bstart+next, bstart+len, null);
//
//        //update after they have been selected because this will modify their order in the curvebag
//        for (int i = bstart; i < bstart+next; i++)
//            updateItem(tx, batch[i], b);
//
//
//        if (toRemove != null)
//            toRemove.forEach(this::remove);
//
//        return next; //# of items actually filled in the array
//    }

    @Override public void top(Consumer<BLink> each) {
        arrayBag.top(each);
    }
    @Override public void topN(int limit, Consumer<BLink> each) {
        arrayBag.topN(limit, each);
    }

    @Override
    public void setCapacity(int c) {
        arrayBag.setCapacity(c);
    }

    /** (utility method specific to curvebag) */
    public boolean isSorted() {
        return arrayBag.isSorted();
    }

    /** (utility method specific to curvebag) */
    public void validate() {
        arrayBag.validate();
    }

    public SortedIndex<BLink<V>> getItems() {
        return arrayBag.items;
    }

    public BLink<V> get(int i) {
        return arrayBag.getItem(i);
    }


    /**
     * Defines the focus curve.  x is a proportion between 0 and 1 (inclusive).
     * x=0 represents low priority (bottom of bag), x=1.0 represents high priority
     *
     * @return
     */

    @FunctionalInterface
    public interface BagCurve extends FloatToFloatFunction {
    }

//    public static class RandomSampler implements ToIntFunction<CurveBag>, Serializable {
//
//        public final BagCurve curve;
//        public final Random rng;
//
//        public RandomSampler(Random rng, BagCurve curve) {
//            this.curve = curve;
//            this.rng = rng;
//        }

    /**
     * maps y in 0..1.0 to an index in [0..size)
     */
    static int index(float y, int size) {
        size--;

        int i = Math.round(y * size);

        if (i > size) return size;
        if (i < 0) return 0;

        return i;
    }

    @Override
    public float getPriorityMin() {
        return arrayBag.getPriorityMin();
    }
    @Override
    public float getPriorityMax() {
        return arrayBag.getPriorityMax();
    }

    /** provides a next index to sample from */
    public final int sampleIndex() {
        int s = size();
        if (s == 1) return 0;

        float x = random.nextFloat();

        BagCurve curve = this.curve;
        float y = curve.valueOf(x);

        int index = index(y, s);
        //System.out.println("\t range:" +  min + ".." + max + " -> f(" + x + ")=" + y + "-> " + index);
        return index;
    }




    public static class CubicBagCurve implements BagCurve {

        @Override
        public final float valueOf(float x) {
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
        public final float valueOf(float x) {
            float nnx = x * x;
            return (nnx * nnx);
        }

        @Override
        public String toString() {
            return "Power4BagCurve";
        }
    }

    public static class Power6BagCurve implements BagCurve {

        @Override
        public final float valueOf(float x) {
            /** x=0, y=0 ... x=1, y=1 */
            float nnx = x * x;
            return (nnx * nnx * nnx);
        }

        @Override
        public String toString() {
            return "Power6BagCurve";
        }
    }

//    /**
//     * Approximates priority -> probability fairness with an exponential curve
//     */
//    @Deprecated
//    public static class FairPriorityProbabilityCurve implements BagCurve {
//
//        @Override
//        public final float valueOf(float x) {
//            return (float) (1.0f - Math.exp(-5.0f * x));
//        }
//
//        @Override
//        public String toString() {
//            return "FairPriorityProbabilityCurve";
//        }
//
//    }

    public static class Power2BagCurve implements BagCurve {

        @Override
        public final float valueOf(float x) {
            return (x * x);
        }

        @Override
        public String toString() {
            return "QuadraticBagCurve";
        }

    }

    //    @Override
//    protected int update(BagTransaction<K, V> tx, V[] batch, int start, int stop, int maxAdditionalAttempts) {
//
//        super.update()
//        int center = this.sampler.applyAsInt(this);
//
//    }
}
