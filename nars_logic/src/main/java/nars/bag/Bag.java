package nars.bag;

import com.google.common.base.Predicate;
import com.google.common.util.concurrent.AtomicDouble;
import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.budget.BudgetSource;
import nars.bag.tx.ForgetNext;
import nars.budget.BudgetFunctions;
import nars.nal.Item;

import java.io.PrintStream;
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
public abstract class Bag<K, V extends Item<K>> extends BudgetSource.DefaultBudgetBuffer implements Iterable<V>, Consumer<V>, Supplier<V> {

    protected final ForgetNext<K, V> forgetNext = new ForgetNext(this);



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

    protected boolean merge(Budget newBudget, Budget oldBudget) {
        return BudgetFunctions.merge(newBudget, oldBudget);
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

    abstract public float mass();

    /**
     * Choose an Item according to distribution policy and take it out of the Bag
     * TODO rename removeNext()
     * @return The selected Item, or null if this bag is empty
     */
    abstract public V pop();

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
        return mass() / size();
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

    /**
     * calls overflow() on an overflown object
     * returns the updated or created concept (not overflow like PUT does (which follows Map.put() semantics)
     */
    public V update(final BagTransaction<K, V> selector) {
        //TODO this is the generic version which may or may not work, or be entirely efficient in some subclasses

        K key = selector.name();
        V item;
        if (key != null) {
            item = get(key);
        }
        else {
            item = peekNext();
        }

        if (item != null) {
            V changed = selector.update(item);
            if (changed == null)
                return item;
            else {
                //it has changed

                //this PUT(TAKE( sequence can be optimized in particular impl
                //the default is a non-optimal failsafe
                remove(item.name());
                item = changed;
            }
        } else {
            item = selector.newItem();
            if (item == null) return null;
        }

        // put the (new or merged) item into itemTable
        final V overflow = put(item);
        if (overflow != null)
            selector.overflow(overflow);

        return item;
    }

    public void printAll() {
        printAll(System.out);
    }

    public void printAll(PrintStream p) {
        Iterator<V> d = iterator();
        while (d.hasNext()) {
            p.println("  " + d.next());
        }
    }



    public float getPrioritySum() {
        int size = size();
        if (size == 0) return 0;
        return getPriorityMean() * size();
    }



    /**
     * allows adjusting forgetting rate in subclasses
     */
    public float getForgetCycles(final float baseForgetCycles, final V item) {
        return baseForgetCycles;
    }

    /**
     * Put an item back into the itemTable
     * <p/>
     * The only place where the forgetting rate is applied
     *
     * @param oldItem The Item to put back
     * @return the item which was removed, or null if none removed
     */
    public V putBack(final V oldItem, final float forgetCycles, final Memory m) {
        if (forgetCycles > 0)
            m.forget(oldItem, getForgetCycles(forgetCycles, oldItem), Global.FORGET_QUALITY_RELATIVE);
        return put(oldItem);
    }

    public V putBack(final V oldItem) {
        return putBack(oldItem, 0, null);
    }

    /**
     * accuracy determines the percentage of items which will be processNext().
     * this is a way to apply the forgetting process applied in putBack.
     */
    public void forgetNext(final float forgetCycles, final float accuracy, final Memory m) {
        final int conceptsToForget = Math.round(size() * accuracy);
        if (conceptsToForget == 0) return;

        forgetNext.set(forgetCycles, m);

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
        forgetNext.set(m.param.cycles(forgetDurations), m);
        update(forgetNext);
        return forgetNext.current;
    }

    public double[] getPriorityDistribution(int bins) {
        return getPriorityDistribution(new double[bins]);
    }

    public double[] getPriorityDistribution(double[] x) {
        int bins = x.length;
        double total = 0;
        for (V e : values()) {
            float p = e.getPriority();
            int b = bin(p, bins - 1);
            x[b]++;
            total++;
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
    public float getMinPriority() {
        float min = 1.0f;
        for (Item e : this) {
            float p = e.getPriority();
            if (p < min) min = p;
        }
        return min;
    }

    /**
     * slow, probably want to override in subclasses
     */
    public float getMaxPriority() {
        float max = 0.0f;
        for (Item e : this) {
            float p = e.getPriority();
            if (p > max) max = p;
        }
        return max;
    }

    public static final int bin(final float x, final int bins) {
        int i = (int) Math.floor((x + 0.5f / bins) * bins);
        return i;
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
