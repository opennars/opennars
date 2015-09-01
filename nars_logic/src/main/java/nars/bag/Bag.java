package nars.bag;

import com.google.common.base.Predicate;
import com.google.common.util.concurrent.AtomicDouble;
import com.gs.collections.api.block.procedure.Procedure2;
import nars.AbstractMemory;
import nars.Memory;
import nars.bag.tx.BagForgetting;
import nars.budget.Budget;
import nars.budget.BudgetSource;
import nars.budget.Itemized;
import org.apache.commons.math3.util.FastMath;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * K=key, V = item/value of type Item
 * <p/>
 * TODO remove unnecessary methods, documetn
 * TODO implement java.util.Map interface
 */
public abstract class Bag<K, V extends Itemized<K>> extends BudgetSource.DefaultBudgetBuffer implements Iterable<V>, Consumer<V>, Supplier<V>, Serializable {

    transient public final BagForgetting<K, V> forgetNext = new BagForgetting<>();



    /** returns the bag to an empty state */
    public abstract void clear();

    /**
     * gets the next value without removing changing it or removing it from any index.  however
     * the bag is cycled so that subsequent elements are different.
     */
    abstract public V peekNext();

    /**
     * TODO rename 'remove'
     * @param key
     * @return
     */
    abstract public V remove(K key);

    /**
     *
     * @param newItem
     * @return null if put was successful, or a displaced item if newItem was inserted.
     * if newItem itself is returned, then it was rejected due to insufficient budget
     * if the newItem already existed, the resulting budget is merged.
     */
    abstract public V put(V newItem);


    Procedure2<Budget,Budget> mergeFunction = Budget.max;

    /** set the merging function to 'average' */
    public void mergeAverage() {  mergeFunction = Budget.average;    }

    /** set the merging function to 'plus' */
    public void mergePlus() {  mergeFunction = Budget.plus;    }

    public void mergeMax() {  mergeFunction = Budget.max;    }

    final protected void merge(final Budget newBudget, final Budget oldBudget) {
        mergeFunction.value(newBudget, oldBudget);
    }

    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    abstract public V get(final K key);

    abstract public Set<K> keySet();

    abstract public int capacity();

    /**
     * Choose an Item according to distribution policy and take it out of the Bag
     * TODO rename removeNext()
     * @return The selected Item, or null if this bag is empty
     */
    abstract public V pop();

    /** called when the bag should be entirely deallocated; differs from clear in that after a clear, the bag should be read yto use again immediately.  here, the bag is being discarded so its data structures should be disassembled thoroughly.  */
    public void delete() {

    }

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    public abstract int size();



    public Iterable<V> values() {
        return this;
    }


    public float getPriorityMean() {
        final int s = size();
        if (s == 0) return 0;
        return getPrioritySum() / s;
    }

    /**
     * iterates all items in (approximately) descending priority
     */
    @Override
    abstract public Iterator<V> iterator();


    /**
     * Check if an item is in the bag.  both its key and its value must match the parameter
     *
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    public boolean contains(final V it) {
        V exist = get(it.name());
        if (exist == null) return false;
        return exist.equals(it);
    }


    /** implements the Consumer<V> interface; invokes a put() */
    @Override public void accept(V v) {
        put(v);
    }


    /** implements the Supplier<V> interface; invokes a remove() */
    @Override public V get() {
        return pop();
    }

    /**
     * if the next item is true via the predicate, then it is TAKEn out of the bag; otherwise the item remains unaffected
     */
    public final V remove(final Predicate<V> iff) {
        V v = peekNext();

        if (v == null) return null;
        if (iff.apply(v)) {
            remove(v.name());
            return v;
        }
        return null;
    }


    public boolean isEmpty() {
        return size() == 0;
    }


    protected final Budget temp = new Budget();

    /**
     * calls overflow() on an overflown object
     * returns the updated or created concept (not overflow like PUT does (which follows Map.put() semantics)
     * NOTE: this is the generic version which may or may not work, or be entirely efficient in some subclasses
     */
    public V update(final BagTransaction<K, V> selector) {


        K key = selector.name();
        V item;
        if (key != null) {
            item = get(key);
        }
        else {
            item = peekNext();
        }

        if (item == null) {
            item = selector.newItem();
            if (item == null) return null;
        } else {

            Budget ib = item.getBudget();

            Budget result = selector.updateItem(item, temp.set(ib));

            if ((result == null) || (result.equalsByPrecision(ib)))
                return item;
            else {
                //it has changed

                //this PUT(TAKE( sequence can be optimized in particular impl
                //the default is a non-optimal failsafe
                remove(item.name());

                //apply changed budget after removed and before re-insert
                ib.set(result);
            }
        }

        // put the (new or merged) item into itemTable
        final V overflow = put(item);
        if (overflow != null)
            selector.overflow(overflow);

//        if (overflow == item)
//            return null;

        return item;
    }

    public void printAll() {
        printAll(System.out);
    }

    public void printAll(PrintStream p) {
        this.forEach( x -> p.println(x.getPriority() + " " + x));
    }



    public float getPrioritySum() {
        final float[] total = {0};
        this.forEach(x -> total[0] += x.getPriority());
        return total[0];
    }



    /**
     * allows adjusting forgetting rate in subclasses
     */
    public float getForgetCycles(final float baseForgetCycles, final V item) {
        return baseForgetCycles;
    }



    /**
     * accuracy determines the percentage of items which will be processNext().
     * should be between 0 and 1.0
     * this is a way to apply the forgetting process applied in putBack.
     */
    public void forgetNext(final float forgetCycles, final float accuracy, final AbstractMemory m) {
        final int conceptsToForget = (int)Math.ceil(size() * accuracy);
        if (conceptsToForget == 0) return;

        forgetNext.set(forgetCycles, m);

        int affected = 0;
        for (int i = 0; i < conceptsToForget; i++) {
            update(forgetNext);
        }

    }

    public void forgetNext(AtomicDouble forgetDurations, final float accuracy, final Memory m) {
        float forgetCycles = m.param.cycles(forgetDurations);
        forgetNext(forgetCycles, accuracy, m);
    }

//    /**
//     * equivalent to a peekNext where forgetting is applied
//     *
//     * @return the variable that was updated, or null if none was taken out
//     * @forgetCycles forgetting time in cycles
//     */
    public V forgetNext(final AtomicDouble forgetDurations, final Memory m) {
        return forgetNext(forgetDurations.floatValue(), m);
    }
    public V forgetNext(final float forgetDurations, final Memory m) {
        setForgetNext(forgetDurations, m);
        return forgetNext();
    }

    public V forgetNext() {
        update(forgetNext);
        return forgetNext.selected;
    }

    /** call this to set the forgetNext settings prior to calling forgetNext() */
    public void setForgetNext(final AtomicDouble forgetDurations, final Memory m) {
        setForgetNext(forgetDurations.floatValue(), m);
    }
    public void setForgetNext(final float forgetDurations, final Memory m) {
        forgetNext.set(m.param.cycles(forgetDurations), m);
    }


    public double[] getPriorityHistogram(int bins) {
        return getPriorityHistogram(new double[bins]);
    }

    public double[] getPriorityHistogram(final double[] x) {
        int bins = x.length;
        forEach(e -> {
            final float p = e.getPriority();
            final int b = bin(p, bins - 1);
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

    public static final int bin(final float x, final int bins) {
        return (int) FastMath.floor((x + (0.5f / bins)) * bins);
    }

    /** finds the mean value of a given bin */
    public static final float unbinCenter(final int b, final int bins) {
        return ((float)b)/bins;
    }

    /** utility function for inserting an item, capturing any overflow,
     * and returning the result of the operation
     * (either the inserted item, or null if rejected).
     * @param n
     * @param selector
     * @return
     */
    protected V putReplacing(final V n, final BagTransaction<K, V> selector) {
        final V overflow = put(n);

        if (overflow!=null) {
            selector.overflow(overflow);

            if (overflow == n) {
                //the bag rejcted the attempt to put this item, so return null
                return null;
            }
        }

        return n; //return the new instance
    }

    /** default implementation; more optimal implementations will avoid instancing an iterator */
    public void forEach(int max, Consumer<V> action) {

        Iterator<V> ii = iterator();
        int n = 0;
        while (ii.hasNext() && n < max) {
            action.accept(ii.next());
            n++;
        }

    }

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

}
