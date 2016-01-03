package nars.bag.impl;

import com.gs.collections.api.block.function.primitive.FloatToFloatFunction;
import nars.Global;
import nars.bag.BagSelector;
import nars.budget.Budget;
import nars.budget.Itemized;
import nars.util.ArraySortedIndex;
import nars.util.data.Util;
import nars.util.data.sorted.SortedIndex;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
public class CurveBag<K, V extends Itemized<K>> extends ArrayBag<K, V> {

    public final static BagCurve xsqrtxBagCurve = new XPowerSqrtXBagCurve();
    public final static BagCurve power6BagCurve = new Power6BagCurve();

    //TODO move sampler features to subclass of CurveBag which specifically provides sampling
    public final BagCurve curve;
    private final Random random;


    public CurveBag(int capacity, Random rng) {
        this(CurveBag.xsqrtxBagCurve, capacity, rng);
    }


    public CurveBag(BagCurve curve, int capacity, Random rng) {
        this(new ArraySortedIndex<>(capacity), curve, rng);

                                /*if (capacity < 128)*/
        //items = new ArraySortedItemList<>(capacity);
                /*else  {
                    //items = new FractalSortedItemList<>(capacity);
                    //items = new RedBlackSortedItemList<>(capacity);
                }*/

    }

    public CurveBag(SortedIndex<V> items, BagCurve curve, Random rng) {
        super(items);

        this.curve = curve;
        this.random = rng;
    }

    public V peekNext(final boolean remove) {

        while (!isEmpty()) {

            final int index = sample();

            final V i = remove ?
                    removeItem(index) : items.get(index);

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


    //TODO
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


    @Override
    final public V peekNext() {
        return peekNext(false);
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
     * optimized peek implementation that scans the curvebag
     * iteratively surrounding a randomly selected point
     */
    @Override
    protected int peekNextFill(BagSelector<K, V> tx, V[] batch, int bstart, int len, int maxAttempts) {


        final int siz = size();
        len = Math.min(siz, len);

        final List<V> a = items.getList();

        int istart;

        if (len != siz) {
            //asking for some of the items
            int r = Math.max(1, len / 2);
            int center = sample();
            istart = center + r; //scan downwards from here (increasing pri order)

            if (r % 2 == 1) istart--; //if odd, give extra room to the start (higher priority)

            //TODO test and if possible use more fair policy that accounts for clipping
            if (istart-r < 0) {
                istart += -(istart-r); //start further below
            }
            if (istart >= siz)
                istart = siz-1;
        }
        else {
            //optimization: asking for all of the items (len==siz)
            //   just add all elements, so dont sample
            istart = siz-1;
        }


        List<K> toRemove = null;

        Budget b = new Budget(); //TODO avoid creating this


        //int bend = bstart + len;
        int next = bstart;

        //scan increasing priority, stopping at the top or if buffer filled
        for (int i = istart; (i >= 0) && (next < len); i--) {
            V v = a.get(i);

            if (v == null) break; //HACK wtf?
            //throw new RuntimeException("null");

            if (v.isDeleted()) {
                if (toRemove == null) toRemove = Global.newArrayList(0); //TODO avoid creating this
                toRemove.add(v.name());
            } else {
                batch[next++] = v;
            }
        }

        //pad with nulls. helpful for garbage collection incase they contain old values (the array is meant to be re-used)
        if (next != len)
            Arrays.fill(batch, bstart+next, bstart+len, null);

        //update after they have been selected because this will modify their order in the curvebag
        for (int i = bstart; i < bstart+next; i++)
            updateItem(tx, batch[i], b);


        if (toRemove != null)
            toRemove.forEach(this::remove);

        return next; //# of items actually filled in the array
    }

    /**
     * Defines the focus curve.  x is a proportion between 0 and 1 (inclusive).
     * x=0 represents low priority (bottom of bag), x=1.0 represents high priority
     *
     * @return
     */

    @FunctionalInterface
    public interface BagCurve extends FloatToFloatFunction, Serializable {
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
     * maps y in 0..1.0 to an index in 0..size
     */
    static final int index(float y, int size) {
       int sizeMin1 = size - 1;
       int i = Math.round(y * size); //invert order = select highest pri most frequently

       if (i >= size) return sizeMin1;
       if (i < 0) return 0;
       return i;
    }


    public final int sample() {
        final int s = size();
        if (s == 1) return 0;

        float x = random.nextFloat();

        //TODO cache these curvepoints when min/max dont change
        final float min = getPriorityMin();
        final float max = getPriorityMax();
        final boolean normalizing = (min != max);
        /*if (normalizing) { //evil ^^
            //rescale to dynamic range
            x = min + (x * (max - min));
        }*/

        final BagCurve curve = this.curve;
        float y = curve.valueOf(x);

        return index(y, s);
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

    public static class XPowerSqrtXBagCurve implements BagCurve {

        @Override
        public final float valueOf(final float x) {

               /*
http://sagecell.sagemath.org/?z=eJxdks1ugzAQhO9IvINvQGNF5h-q0mfIPYosmkKClEKCqQpv37WdaruGy8zoQ5rdpQ_XqFlf1GNeQPme741SyLg
RWsQysSKRqRWpzKzIZG5FLgsrCllaUcrKikrWVtQyFlrZt59mNrBhZHM7XrowEUJEr77H4Fkla3T8OX2FkY22pg9X-TRDz7Z3wQBg21sj9vHzO_2Y3rsm_k
_uY2QTwsJoLpsgmxIWpnfZFNmMsLAgl82QzQkLO3TZHNmCsLBmly2QLQkLl3DZEtmKsHAsl62QrQkL93TZ-o-ll9AHNyiGvnefh3GxZ0IDd0ADi0YDm0QDq
0IDu0ADw6KBadBAXTS6ka5zaO4TJOHxBL_UYffnBDe9aBhz04-GCTc9aZhy05eGGTe9aZhz05-GBTdz0LDkZh4aVtzMRcOa2_l0qq7TT3i_TUt44KxdOyVv
7Ud3U80xgFVM87BsLBRsUOw6XK6dWqKAs6A9nzulWD93j-9uPG_BKYp-Ab9qBwE=&lang=sage*/

 /*
f(x)=x*sqrt(x)

n_0_1=0
n_1_2=0
n_2_3=0
n_3_4=0
n_4_5=0
n_5_6=0
n_6_7=0
n_7_8=0
n_8_9=0
n_9_10=0

for i in range(2000):
    x_ = random()
    y=f(x_)
    if y>0 and y<=0.1:
        n_0_1+=1
    if y>0.1 and y<=0.2:
        n_1_2+=1
    if y>0.2 and y<=0.3:
        n_2_3+=1
    if y>0.3 and y<=0.4:
        n_3_4+=1
    if y>0.4 and y<=0.5:
        n_4_5+=1
    if y>0.5 and y<=0.6:
        n_5_6+=1
    if y>0.6 and y<=0.7:
        n_6_7+=1
    if y>0.7 and y<=0.8:
        n_7_8+=1
    if y>0.8 and y<=0.9:
        n_8_9+=1
    if y>0.9 and y<=1:
        n_9_10+=1

print n_0_1
print n_1_2
print n_2_3
print n_3_4
print n_4_5
print n_5_6
print n_6_7
print n_7_8
print n_8_9
print n_9_10

P=point([])
P+=point([0,n_0_1])
P+=point([1,n_1_2])
P+=point([2,n_2_3])
P+=point([3,n_3_4])
P+=point([4,n_4_5])
P+=point([5,n_5_6])
P+=point([6,n_6_7])
P+=point([7,n_7_8])
P+=point([8,n_8_9])
P+=point([9,n_9_10])
show(plot(P, axes_labels=['priority (0 is highest)', 'access frequency']))
             */

            return (float) (x * Math.sqrt(x));
        }

        @Override
        public String toString() {
            return "Power2BagCurve";
        }
    }

    public static class Power2BagCurve implements BagCurve {

        @Override
        public final float valueOf(final float x) {
            return x * x;
        }

        @Override
        public String toString() {
            return "Power2BagCurve";
        }
    }

    public static class Power4BagCurve implements BagCurve {

        @Override
        public final float valueOf(final float x) {
            return x * x * x * x;
        }

        @Override
        public String toString() {
            return "Power4BagCurve";
        }
    }

    public static class Power6BagCurve implements BagCurve {

        @Override
        public final float valueOf(final float x) {
            /** x=0, y=0 ... x=1, y=1 */
            //return x;

            float nx = x;
            return (nx * nx * nx * nx * nx * nx);

            // return (float) (1.0f/(1.0f+Math.exp(-(10.0f*(x-0.5f)))));

            //return x*x; //linear for now

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

    //    @Override
//    protected int update(BagTransaction<K, V> tx, V[] batch, int start, int stop, int maxAdditionalAttempts) {
//
//        super.update()
//        int center = this.sampler.applyAsInt(this);
//
//    }
}
