package nars.bag;

import com.google.common.collect.Sets;
import com.gs.collections.api.block.procedure.Procedure2;
import nars.bag.impl.AbstractCacheBag;
import nars.budget.Budget;
import nars.budget.Itemized;
import nars.budget.UnitBudget;
import nars.util.data.Util;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * K=key, V = item/value of type Item
 * <p>
 * TODO remove unnecessary methods, documetn
 * TODO implement java.util.Map interface
 */
public abstract class Bag<K, V extends Itemized<K>> extends AbstractCacheBag<K, V> implements Consumer<V>, Supplier<V>, Iterable<V>, Externalizable {


    protected Procedure2<Budget, Budget> mergeFunction;

    public static final <V> boolean bufferIncludes(V[] buffer, V item) {
        for (V x : buffer) {
            if (x == null)
                break;
            if (x == item) return true;
        }
        return false;
    }

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
    public abstract V remove(K key);



    /**
     * @param newItem
     * @return null if put was successful, or a displaced item if newItem was inserted.
     * if newItem itself is returned, then it was rejected due to insufficient budget
     * if the newItem already existed, the resulting budget is merged.
     */
    public abstract V put(V newItem);

    @Override
    @Deprecated public V put(K k, V v) {
        throw new RuntimeException("depr");
    }

    public void setMergeFunction(Procedure2<Budget, Budget> mergeFunction) {
        this.mergeFunction = mergeFunction;
    }

    /**
     * set the merging function to 'average'
     */
    public Bag mergeAverage() {
        setMergeFunction(UnitBudget.average);
        return this;
    }

    /**
     * set the merging function to 'plus'
     */
    public Bag mergePlus() {
        setMergeFunction(UnitBudget.plus);
        return this;
    }

    public Bag mergeMax() {
        setMergeFunction(UnitBudget.max);
        return this;
    }

    protected final void merge(Budget newBudget, Budget oldBudget) {
        mergeFunction.value(newBudget, oldBudget);
    }

    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    @Override
    public abstract V get(K key);

    public abstract Set<K> keySet();

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
        V exist = get(it.name());
        if (exist == null) return false;
        return exist.equals(it);
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Bag)
            return Bag.equals(this, ((Bag)obj));
        return false;
    }

    /**  by value set */
    public static <K,V extends Itemized<K>> boolean equals(Bag<K, V> a, Bag<K, V> b) {
        if (a.size()!=b.size()) return false;
        HashSet<V> aa = Sets.newHashSet(a);
        HashSet<V> bb = Sets.newHashSet(b);

        //TODO test for budget equality, which must be done separately
        return aa.equals(bb);
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
     * implements the Consumer<V> interface; invokes a put()
     */
    @Override
    public void accept(V v) {
        put(v);
    }

    /**
     * implements the Supplier<V> interface; invokes a remove()
     */
    @Override
    public V get() {
        return pop();
    }

    public boolean isEmpty() {
        return size() == 0;
    }



    public void printAll() {
        printAll(System.out);
    }

    public void printAll(PrintStream p) {
        forEach(x -> p.println(x.getPriority() + " " + x));
    }

    /**
     * should visit items highest priority first, if possible.
     * for some bags this may not be possible.
     */
    //by default this will use iterator().forEach() but this can be used to make sure each implementation offers its best
    //@Override abstract public void forEach(final Consumer<? super V> action);
    public float getPrioritySum() {
        float[] total = {0};
        forEach(x -> total[0] += x.getPriority());
        return total[0];
    }


//    final public int forgetNext(float forgetCycles, final V[] batch, final long now) {
//        return forgetNext(forgetCycles, batch, 0, batch.length, now, batch.length/2 /* default to max 1.5x */);
//    }

    /** warning: slow */
    public double getStdDev(StandardDeviation target) {
        target.clear();
        forEach(x -> target.increment(x.getPriority()));
        return target.getResult();
    }




    @Override
    public String toString() {
        return getClass().getSimpleName();// + "(" + size() + "/" + getCapacity() +")";
    }

    /**
     * slow, probably want to override in subclasses
     */
    public float getPriorityMin() {
        float min = 1.0f;
        for (Itemized e : this) {
            float p = e.getPriority();
            if (p < min) min = p;
        }
        return min;
    }

    /**
     * slow, probably want to override in subclasses
     */
    public float getPriorityMax() {
        float max = 0.0f;
        for (Itemized e : this) {
            float p = e.getPriority();
            if (p > max) max = p;
        }
        return max;
    }


    /**
     * utility function for inserting an item, capturing any overflow,
     * and returning the result of the operation
     * (either the inserted item, or null if rejected).
     *
     * @param n
     * @param selector
     * @return
     */
    protected V putReplacing(V n, BagSelector<K, V> selector) {
        V overflow = put(n);

        if (overflow != null) {
            selector.overflow(overflow);

            if (overflow == n) {
                //the bag rejcted the attempt to put this item, so return null
                return null;
            }
        }

        return n; //return the new instance
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

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int num = in.readInt();
        for (int i = 0; i < num; i++) {
            put((V) in.readObject());
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size());

        //TODO use forEach if it can do the right order

        forEach(v -> {
            try {
                out.writeObject(v);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public double[] getPriorityHistogram(int bins) {
        return getPriorityHistogram(new double[bins]);
    }

    public double[] getPriorityHistogram(double[] x) {
        int bins = x.length;
        forEach(e -> {
            float p = e.getPriority();
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
