package nars.bag;

import com.google.common.util.concurrent.AtomicDouble;
import com.gs.collections.api.block.procedure.Procedure2;
import nars.Memory;
import nars.bag.impl.AbstractCacheBag;
import nars.bag.tx.BagForgetting;
import nars.budget.Budget;
import nars.budget.Itemized;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * K=key, V = item/value of type Item
 * <p/>
 * TODO remove unnecessary methods, documetn
 * TODO implement java.util.Map interface
 */
public abstract class Bag<K, V extends Itemized<K>> extends AbstractCacheBag<K,V> implements Consumer<V>, Supplier<V>  {

    transient final BagForgetting<K, V> forgetNext = new BagForgetting<>();

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

    /** not used currently in Bag classes, but from CacheBag interface */
    @Override public Consumer<V> getOnRemoval() {  return null;    }
    /** not used currently in Bag classes, but from CacheBag interface */
    @Override public void setOnRemoval(Consumer<V> onRemoval) { }

    /** implements the Consumer<V> interface; invokes a put() */
    @Override public void accept(V v) {
        put(v);
    }


    /** implements the Supplier<V> interface; invokes a remove() */
    @Override public V get() {
        return pop();
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

            if ((result == null) || (result.isDeleted()) || (result.equalsByPrecision(ib)))
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


    /** faster than using the BagTransaction version when creation of instances is not necessary */
    public V peekNext(final BagSelector<K, V> selector) {

        V item = peekNext();

        if (item == null)
            return null;

        Budget ib = item.getBudget();

        Budget result = selector.updateItem(item, temp.set(ib));

        if ((result == null) || (result.isDeleted()) || (result.equalsByPrecision(ib)))
            return item;
        else {
            //it has changed

            //this PUT(TAKE( sequence can be optimized in particular impl
            //the default is a non-optimal failsafe
            remove(item.name());

            //apply changed budget after removed and before re-insert
            ib.set(result);
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

    /** should visit items highest priority first, if possible.
     *  for some bags this may not be possible.
     */
    //by default this will use iterator().forEach() but this can be used to make sure each implementation offers its best
    //@Override abstract public void forEach(final Consumer<? super V> action);




    public float getPrioritySum() {
        final float[] total = {0};
        this.forEach(x -> total[0] += x.getPriority());
        return total[0];
    }

    public double getStdDev(StandardDeviation target) {
        target.clear();
        this.forEach( x -> target.increment( x.getPriority() ) );
        return target.getResult();
    }




//    final public int forgetNext(float forgetCycles, final V[] batch, final long now) {
//        return forgetNext(forgetCycles, batch, 0, batch.length, now, batch.length/2 /* default to max 1.5x */);
//    }

    /** collects a batch of values and returns them after applying forgetting to each
     *  returns number of items collected.
     *  batch[] will be overwritten between start and stop,
     *  but may end before stop.
     *  use the returned count to determine what the next
     *  available position is.
     *
     * maxAdditionalAttempts is the number of retries beyond the given
     * (stop-start) amount of entries in case the bag or selector
     * refuse to provide one in that iteration.  (ex: novelty
     * filtering.)
     *
     * the produced list may contain duplicates.
     * TODO make a non-duplicate Set based version of this
     * */
    public int forgetNext(float forgetCycles, V[] batch, final int start, final int stop, final long now, final int maxAdditionalAttempts) {
        if (isEmpty())
            return 0;

        return peekNext(forgetNext.set(forgetCycles, now),
                batch, start, stop, maxAdditionalAttempts);
    }

    /** receive a batch (up to specific) of values.
     *  implementations can override this to
     *  optimize it for its unique data management patterns.
     *
     *  the length is specified by the length of a provider array
     *  where the data values will be stored before returning.
     *  @return the number of entries filled in the array
     *  remaining entries will be null
     *
     *  the transaction(s) themseles will have completed by the
     *  time the values are returned.
     *
     *  TODO option for if duplicates are allowed
     */
    protected int peekNext(final BagSelector<K, V> tx, V[] batch, int start, int stop, int maxAdditionalAttempts) {

        final int batchlen = Math.min(size(), stop-start);
        final int maxAttempts = batchlen + maxAdditionalAttempts;

        return peekNextFill(tx, batch, start, batchlen, maxAttempts);
    }

    protected int peekNextFill(BagSelector<K, V> tx, V[] batch, int start, int len, int maxAttempts) {
        int fill = 0;

        final Function<V, BagSelector.ForgetAction> filter = tx.getModel();

        if (len == size()) {
            //optimization: if len==s then just add all elements

            for (V x : values()) {

                //HACK
                BagSelector.ForgetAction p = (filter == null) ? BagSelector.ForgetAction.Select : filter.apply(x);

                if ((p!= BagSelector.ForgetAction.Ignore) && (p!= BagSelector.ForgetAction.IgnoreAndForget))
                    batch[start + (fill++)] = x;
            }

            return fill;
        }

        for (int i = 0; (fill < len) && (i < maxAttempts); i++) {
            final V x = peekNext(tx);
            if (x != null) {

                //HACK
                BagSelector.ForgetAction p = (filter == null) ? BagSelector.ForgetAction.Select : filter.apply(x);

                if ((p!= BagSelector.ForgetAction.Ignore) && (p!= BagSelector.ForgetAction.IgnoreAndForget))
                    if (!bufferIncludes(batch, x)) {
                        batch[start + (fill++)] = x;
                }
            }
        }

        return fill;
    }

    public final static <V> boolean bufferIncludes(V[] buffer, V item) {
        for (final V x : buffer) {
            if (x == null)
                break;
            if (x == item) return true;
        }
        return false;
    }


    /**
     * accuracy determines the percentage of items which will be processNext().
     * should be between 0 and 1.0
     * this is a way to apply the forgetting process applied in putBack.
     */
    public void forgetNext(final float forgetCycles, final float accuracy, final Memory m) {
        final int conceptsToForget = (int)Math.ceil(size() * accuracy);
        if (conceptsToForget == 0) return;

        forgetNext.set(forgetCycles, m.time());

        for (int i = 0; i < conceptsToForget; i++) {
            peekNext(forgetNext);
        }

    }

    final public void forgetNext(AtomicDouble forgetDurations, final float accuracy, final Memory m) {
        float forgetCycles = m.durationToCycles(forgetDurations);
        forgetNext(forgetCycles, accuracy, m);
    }

//    /**
//     * equivalent to a peekNext where forgetting is applied
//     *
//     * @return the variable that was updated, or null if none was taken out
//     * @forgetCycles forgetting time in cycles
//     */
    final public V forgetNext(final AtomicDouble forgetDurations, final Memory m) {
        return forgetNext(forgetDurations.floatValue(), m);
    }

    public V forgetNext(final float forgetDurations, final Memory m) {
        setForgetNext(forgetDurations, m);
        return forgetNext();
    }

    public V forgetNext() {
        peekNext(forgetNext);
        return forgetNext.lastForgotten;
    }

    /** call this to set the forgetNext settings prior to calling forgetNext() */
    protected void setForgetNext(final float forgetDurations, final Memory m) {
        forgetNext.set(m.durationToCycles(forgetDurations), m.time());
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


    /** utility function for inserting an item, capturing any overflow,
     * and returning the result of the operation
     * (either the inserted item, or null if rejected).
     * @param n
     * @param selector
     * @return
     */
    protected V putReplacing(final V n, final BagSelector<K,V> selector) {
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


    final public int peekNext(BagSelector<K,V> tx, V[] result, int additionalAttempts) {
        return peekNext(tx, result, 0, result.length, additionalAttempts);
    }

    abstract public void setCapacity(int c);

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
