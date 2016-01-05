package nars.bag;

import nars.bag.impl.AbstractCacheBag;
import nars.budget.Budget;
import nars.budget.BudgetMerge;
import nars.util.data.Util;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * K=key, V = item/value of type Item
 * <p>
 * TODO remove unnecessary methods, documetn
 * TODO implement java.util.Map interface
 */
public abstract class Bag<V> extends AbstractCacheBag<V, BLink<V>> implements Consumer<V>, Supplier<BLink<V>>, Iterable<V> {


    protected BudgetMerge mergeFunction = null;


    /**
     * returns the bag to an empty state
     */
    @Override
    public abstract void clear();

    /**
     * gets the next value without removing changing it or removing it from any index.  however
     * the bag is cycled so that subsequent elements are different.
     */
    public abstract BLink<V> sample();


    /**
     * TODO rename 'remove'
     *
     * @param key
     * @return
     */
    @Override
    public abstract BLink<V> remove(V key);


    /**
     * put with an empty budget
     */
    public abstract BLink<V> put(Object newItem);

    public final BLink<V> put(Object i, Budget b) {
        return put(i, b, 1f);
    }

    @Override
    public BLink<V> put(V v, BLink<V> b) {
        return put(v, b, 1f);
    }

    public abstract BLink<V> put(Object i, Budget b, float scale);




//    /**
//     * iterates in sequence starting from the top until predicate returns false, limited by max iterations (n)
//     */
//    abstract public void top(int n, Predicate<BagBudget<V>> each);
//        int[] toFire = { n };
//        top(c -> (each.test(c) && (toFire[0]--) > 0));


    public void sample(int n, Collection<BLink<V>> target) {
        sample(n, null, target);
    }

    public abstract Bag<V> sample(int n, Predicate<BLink> each, Collection<BLink<V>> target);
//    /**
//     * fills a collection with at-most N items, if an item passes the predicate.
//     * returns how many items added
//     */
//    public int sample(int n, Predicate<BagBudget<V>> each, Collection<BagBudget<V>> target) {
//        int startSize = target.size();
//        sample(n, x -> {
//            if (each.test(x)) {
//                target.add(x);
//            }
//            return true;
//        });
//        return target.size() - startSize;
//    }


    public Bag<V> setMergeFunction(BudgetMerge mergeFunction) {
        this.mergeFunction = mergeFunction;
        return this;
    }


    /**
     * set the merging function to 'plus'
     */
    public Bag<V> mergePlus() {
        return setMergeFunction(BudgetMerge.plusDQDominated);
    }

    /**
     * sets a null merge function, which can be used to detect
     * merging which should not happen (it will throw null pointer exception)
     */
    public Bag<V> mergeNull() {
        return setMergeFunction(null);
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
    public abstract BLink<V> pop();

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

    public float getSummaryMean() {
        int s = size();
        if (s == 0) return 0;
        return getSummarySum() / s;
    }

//    /** not used currently in Bag classes, but from CacheBag interface */
//    @Override public Consumer<V> getOnRemoval() {  return null;    }
//    /** not used currently in Bag classes, but from CacheBag interface */
//    @Override public void setOnRemoval(Consumer<V> onRemoval) { }

    /**
     * iterates all items in (approximately) descending priority
     * forEach may be used to avoid allocation of iterator instances
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
        return get(it) != null;
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


    /**
     * commits the next set of changes and updates any sorting
     */
    public abstract void commit();

    /**
     * implements the Consumer<V> interface; invokes a put()
     */
    @Override
    public final void accept(V v) {
        put(v);
    }

    /**
     * implements the Supplier<V> interface; invokes a remove()
     */
    @Override
    public final BLink<V> get() {
        return pop();
    }

    public final boolean isEmpty() {
        return size() == 0;
    }

    public void printAll() {
        printAll(System.out);
    }

    public void printAll(PrintStream p) {
        top(b -> p.println(b.toBudgetString() + ' ' + b.get()));
    }

    /**
     * should visit items highest priority first, if possible.
     * for some bags this may not be possible.
     */
    //by default this will use iterator().forEach() but this can be used to make sure each implementation offers its best
    //@Override abstract public void forEach(final Consumer<? super V> action);
    public float getPrioritySum() {
        float[] total = {0};
        top(v -> total[0] += v.getPriority());
        return total[0];
    }

    public float getSummarySum() {
        float[] total = {0};
        top(v -> total[0] += v.summary());
        return total[0];
    }

    @Deprecated
    public void top(Consumer<BLink> each) {
        topWhile(e -> {
            each.accept(e);
            return true;
        });
    }

    /**
     * if predicate evaluates false, it terminates the iteration
     */
    public abstract void topWhile(Predicate<BLink> each);

    //TODO provide default impl
    public abstract void topN(int limit, Consumer<BLink> each);


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
        float[] min = {Float.POSITIVE_INFINITY};
        top(b -> {
            float p = b.getPriority();
            if (p < min[0]) min[0] = p;
        });
        return min[0];
    }

    /**
     * slow, probably want to override in subclasses
     */
    public float getPriorityMax() {
        float[] max = {Float.NEGATIVE_INFINITY};
        top(b -> {
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
        while (ii.hasNext() && (n++ < max)) {
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
        top(budget -> {
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
