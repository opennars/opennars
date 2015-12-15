package nars.bag;

import nars.bag.impl.AbstractCacheBag;
import nars.budget.Budget;
import nars.util.data.Util;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static nars.Global.BUDGET_EPSILON;


/**
 * K=key, V = item/value of type Item
 * <p>
 * TODO remove unnecessary methods, documetn
 * TODO implement java.util.Map interface
 */
public abstract class Bag<V> extends AbstractCacheBag<V,BagBudget<V>> implements Consumer<V>, Supplier<V>, Iterable<V> {


    public interface BudgetMerge {
        void merge(Budget target, Budget src, float srcScale);
    }

    protected BudgetMerge mergeFunction;


    /**
     * returns the bag to an empty state
     */
    @Override
    public abstract void clear();

    /**
     * gets the next value without removing changing it or removing it from any index.  however
     * the bag is cycled so that subsequent elements are different.
     */
    public abstract V peekNext();

    /**
     * TODO rename 'remove'
     *
     * @param key
     * @return
     */
    @Override
    public abstract BagBudget<V> remove(V key);



    /**
     * put with an empty budget
     */
    public abstract BagBudget<V> put(Object newItem);


    public final BagBudget<V> put(Object i, Budget b) {
        return put(i, b, 1f);
    }

    @Override
    public BagBudget<V> put(V v, BagBudget<V> b) {
        return put(v, b, 1f);
    }

    public abstract BagBudget<V> put(Object i, Budget b, float scale);


    public void setMergeFunction(BudgetMerge mergeFunction) {
        this.mergeFunction = mergeFunction;
    }

    public static BudgetMerge plus = (target, src, srcScale) -> {
        float dp = src.getPriority() * srcScale;

        float currentPriority = target.getPriorityIfNaNThenZero();

        float nextPriority = Math.min(1,currentPriority + dp);

        float currentNextPrioritySum = (currentPriority + nextPriority);

        /* current proportion */ float cp = (Util.equal(currentNextPrioritySum, 0, BUDGET_EPSILON)) ?
                0.5f : /* both are zero so they have equal infleunce */
                (currentPriority / currentNextPrioritySum);
        /* next proportion */ float np = 1.0f - cp;


        float D = target.getDurability();
        float Q = target.getQuality();

        target.set(
            nextPriority,
            Math.max(D, (cp * D) + (np * src.getDurability())),
            Math.max(Q, (cp * Q) + (np * src.getQuality()))
        );
    };



    /**
     * set the merging function to 'plus'
     */
    public Bag mergePlus() {
        setMergeFunction(plus);
        return this;
    }


    protected final void merge(Budget target, Budget src, float scale) {
        mergeFunction.merge(target, src, scale);
    }

//    /**
//     * Get an Item by key
//     *
//     * @param key The key of the Item
//     * @return The Item with the given key
//     */
//    @Override
//    public abstract V get(K key);

//    public abstract Set<K> keySet();

    public abstract int capacity();

    /**
     * Choose an Item according to distribution policy and take it out of the Bag
     * TODO rename removeNext()
     *
     * @return The selected Item, or null if this bag is empty
     */
    public abstract V pop();

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    @Override
    public abstract int size();

    public Iterable<V> values() {
        return this;
    }

    public float getPriorityMean() {
        int s = size();
        if (s == 0) return 0;
        return getPrioritySum() / s;
    }

//    /** not used currently in Bag classes, but from CacheBag interface */
//    @Override public Consumer<V> getOnRemoval() {  return null;    }
//    /** not used currently in Bag classes, but from CacheBag interface */
//    @Override public void setOnRemoval(Consumer<V> onRemoval) { }

    /**
     * iterates all items in (approximately) descending priority
     */
    @Override
    public abstract Iterator<V> iterator();

    /**
     * Check if an item is in the bag.  both its key and its value must match the parameter
     *
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    public boolean contains(V it) {
        return get(it)!=null;
    }


    //    /**
//     * if the next item is true via the predicate, then it is TAKEn out of the bag; otherwise the item remains unaffected
//     */
//    public final V remove(final Predicate<V> iff) {
//        V v = peekNext();
//
//        if (v == null) return null;
//        if (iff.apply(v)) {
//            remove(v.name());
//            return v;
//        }
//        return null;
//    }

    /** update the entire bag */
    abstract public void update();


    /**
     * implements the Consumer<V> interface; invokes a put()
     */
    @Override
    final public void accept(V v) {
        put(v);
    }

    /**
     * implements the Supplier<V> interface; invokes a remove()
     */
    @Override
    final public V get() {
        return pop();
    }

    final public boolean isEmpty() {
        return size() == 0;
    }

    public void printAll() {
        printAll(System.out);
    }

    public void printAll(PrintStream p) {
        forEachEntry(b -> p.println(b.toBudgetString() + " " + b.get()));
    }

    /**
     * should visit items highest priority first, if possible.
     * for some bags this may not be possible.
     */
    //by default this will use iterator().forEach() but this can be used to make sure each implementation offers its best
    //@Override abstract public void forEach(final Consumer<? super V> action);
    public float getPrioritySum() {
        float[] total = {0};
        forEachEntry(v -> total[0] += v.getPriority());
        return total[0];
    }


    abstract public void forEachEntry(Consumer<BagBudget> each);

//    final public int forgetNext(float forgetCycles, final V[] batch, final long now) {
//        return forgetNext(forgetCycles, batch, 0, batch.length, now, batch.length/2 /* default to max 1.5x */);
//    }

//    /** warning: slow */
//    public double getStdDev(StandardDeviation target) {
//        target.clear();
//        forEachEntry(x -> target.increment(x.getPriority()));
//        return target.getResult();
//    }




    @Override
    public String toString() {
        return getClass().getSimpleName();// + "(" + size() + "/" + getCapacity() +")";
    }

    /**
     * slow, probably want to override in subclasses
     */
    public float getPriorityMin() {
        float[] min = { Float.POSITIVE_INFINITY };
        forEachEntry(b -> {
            float p = b.getPriority();
            if (p < min[0]) min[0] = p;
        });
        return min[0];
    }
    /**
     * slow, probably want to override in subclasses
     */
    public float getPriorityMax() {
        float[] max = { Float.NEGATIVE_INFINITY};
        forEachEntry(b -> {
            float p = b.getPriority();
            if (p > max[0]) max[0] = p;
        });
        return max[0];
    }


    /**
     * default implementation; more optimal implementations will avoid instancing an iterator
     */
    public void forEach(int max, Consumer<? super V> action) {

        Iterator<V> ii = iterator();
        int n = 0;
        while (ii.hasNext() && n++ < max) {
            action.accept(ii.next());
        }

    }



    public abstract void setCapacity(int c);


//    /**
//     * for bags which maintain a separate name index from the items, more fine-granied access methods to avoid redundancy when possible
//     */
//    @Deprecated
//    abstract public static class IndexedBag<E extends Item<K>, K> extends Bag<K, E> {
//
//        public E TAKE(final K key) {
//            return take(key, true);
//        }
//
//
//        /**
//         * registers the item
//         */
//        abstract protected void index(E value);
//
//        /**
//         * unregisters it
//         */
//        abstract protected E unindex(K key);
//
//        protected E unindex(E e) {
//            return unindex(e.name());
//        }
//
//        abstract public E take(final K key, boolean unindex);
//
//        public E TAKE(E value) {
//            return TAKE(value.name());
//        }
//
//
//        /**
//         * Insert an item into the itemTable, and return the overflow
//         *
//         * @param newItem The Item to put in
//         * @return null if nothing was displaced and if the item itself replaced itself,
//         * or the The overflow Item if a different item had to be removed
//         */
//        abstract protected E addItem(final E newItem, boolean index);
//
//        public E PUT(final E newItem) {
//            return addItem(newItem, true);
//        }
//
//
//        /**
//         * Add a new Item into the Bag via a BagSelector interface for lazy or cached instantiation of Bag items
//         *
//         * @return the item which was removed, which may be the input item if it could not be inserted; or null if nothing needed removed
//         * <p/>
//         * WARNING This indexing-avoiding version not completely working yet, so it is not used as of this commit
//         */
//
//        public E putInFast(BagSelector<K, E> selector) {
//
//            E item = take(selector.name(), false);
//
//            if (item != null) {
//                item = (E) item.merge(selector);
//                final E overflow = addItem(item, false);
//                if (overflow == item) {
//                    unindex(item.name());
//                }
//                return overflow;
//            } else {
//                item = selector.newItem();
//
//                //compare by reference, sanity check
//                if (item.name() != selector.name())
//                    throw new RuntimeException("Unrecognized selector and resulting new instance have different name()'s: item=" + item.name() + " selector=" + selector.name());
//
//                // put the (new or merged) item into itemTable
//                return PUT(item);
//            }
//
//
//        }
//    }

    public double[] getPriorityHistogram(int bins) {
        return getPriorityHistogram(new double[bins]);
    }

    public double[] getPriorityHistogram(double[] x) {
        int bins = x.length;
        forEachEntry(budget -> {
            float p = budget.getPriority();
            int b = Util.bin(p, bins - 1);
            x[b]++;
        });
        double total = 0;
        for (double e : x) {
            total += e;
        }
        if (total > 0) {
            for (int i = 0; i < bins; i++)
                x[i] /= total;
        }
        return x;
    }

}
