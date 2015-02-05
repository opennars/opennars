package nars.util.bag;

import com.google.common.base.Predicate;
import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.Item;
import nars.util.bag.select.ForgetNext;
import reactor.jarjar.jsr166e.extra.AtomicDouble;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;


/** K=key, V = item/value of type Item
 *
 * TODO remove unnecessary methods, documetn
 * */
public abstract class Bag<K, V extends Item<K>> implements Iterable<V> {

    protected final ForgetNext<K,V> forgetNext = new ForgetNext(this);


    public interface MemoryAware {
        public void setMemory(Memory m);
    }

    public static final int bin(final float x, final int bins) {
        int i = (int)Math.floor((x + 0.5f/bins) * bins);
        return i;
    }


    public abstract void clear();   

    /**
     * Check if an item is in the bag.  both its key and its value must match the parameter
     *
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    public boolean contains(final V it) {
        V exist = GET(it.name());
        return exist.equals(it);
    }
    
    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    abstract public V GET(final K key);
    
    abstract public Set<K> keySet();

    abstract public int getCapacity();

    abstract public float getMass();

    /**
     * Choose an Item according to distribution policy and take it out of the Bag
     * @return The selected Item, or null if this bag is empty
     */
    abstract public V TAKENEXT();


    /** if the next item is true via the predicate, then it is TAKEn out of the bag; otherwise the item remains unaffected */
    public V TAKENEXT(Predicate<V> iff) {
        V v = PEEKNEXT();
        if (v == null) return null;
        if (iff.apply(v)) {
            TAKE(v);
            return v;
        }
        return null;
    }
    

    /** gets the next value without removing changing it or removing it from any index.  however
     the bag is cycled so that subsequent elements are different. */    
    abstract public V PEEKNEXT();


    public boolean isEmpty() {
        return size() == 0;
    }

    abstract public V TAKE(K key);

    public V TAKE(V item) {
        return TAKE(item.name());
    }

    abstract public V PUT(V newItem);

//    /**
//     * Add a new Item into the Bag
//     *
//     * @param newItem The new Item
//     * @return the item which was removed, which may be the input item if it could not be inserted; or null if nothing needed removed
//     */
//    public V PUT(V newItem) {
//
//        final V existingItemWithSameKey = TAKE(newItem);
//
//        V item;
//        if (existingItemWithSameKey != null) {
//            item = (V)existingItemWithSameKey.merge(newItem);
//        }
//        else {
//            item = newItem;
//        }
//
//        // put the (new or merged) item into itemTable
//        final V overflowItem = PUT(item);
//
//
//        if (overflowItem!=null)
//            return overflowItem;
//
//        return null;
//    }


    /** returns the updated or created concept (not overflow like PUT does (which follows Map.put() semantics) */
    public V UPDATE(final BagSelector<K, V> selector) {
        //TODO this is the generic version which may or may not work, or be entirely efficient in some subclasses

        V item = GET(selector.name()); //

        if (item != null) {
            V changed = selector.update(item);
            if (changed == null)
                return item;
            else {
                //it has changed

                //this PUT(TAKE( sequence can be optimized in particular impl
                //the default is a non-optimal failsafe
                TAKE(item);
                item = changed;
            }
        }
        else {
            item = selector.newItem();
            if (item == null) return null;
        }

        // put the (new or merged) item into itemTable
        final V overflow = PUT(item);
        if (overflow!=null)
            selector.overflow(overflow);

        return item;
    }


//    /**
//     * Add a new Item into the Bag via a BagSelector interface for lazy or cached instantiation of Bag items
//     *
//     * @return the item which was removed,
//     * which may be the input item if it could not be inserted;
//     * or null if nothing needed removed.
//     *
//     * this return value follows the Map.put() semantics
//     */
//    @Deprecated public V PUT(final BagSelector<K, V> selector) {
//
//        final K key = selector.name();
//        if (key == null) return null;
//        V item = GET(key);
//
//        if (item != null) {
//            V itemChanged = selector.update(item);
//            if (itemChanged != null) {
//                //this PUT(TAKE( sequence can be optimized in particular impl
//                //the default is a non-optimal failsafe
//                final V overflow = PUT(TAKE(item));
//                if (overflow!=null)
//                    selector.overflow(overflow);
//                return overflow;
//            }
//        }
//        else {
//            item = selector.newItem();
//            if (item!=null)
//                return PUT(item); // put the (new or merged) item into itemTable
//        }
//
//        return null;
//    }



    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    public abstract int size();
    
    
    public void printAll(PrintStream p) {
        Iterator<V> d = iterator();
        while (d.hasNext()) {
            p.println("  " + d.next() );
        }
    }
    
    abstract public Iterable<V> values();

    public abstract float getAveragePriority();

    public float getTotalPriority() {
        int size = size();
        if (size == 0) return 0;
        return getAveragePriority() * size();
    }

    /** iterates all items in (approximately) descending priority */
    @Override public abstract Iterator<V> iterator();
    
    /** allows adjusting forgetting rate in subclasses */    
    public float getForgetCycles(final float baseForgetCycles, final V item) {
        return baseForgetCycles;
    }
    


    /**
     * Put an item back into the itemTable
     * <p>
     * The only place where the forgetting rate is applied
     *
     * @param oldItem The Item to put back
     * @return the item which was removed, or null if none removed
     */    
    public V putBack(final V oldItem, final float forgetCycles, final Memory m) {
        if (forgetCycles > 0)
            m.forget(oldItem, getForgetCycles(forgetCycles, oldItem), Parameters.FORGET_QUALITY_RELATIVE);
        return PUT(oldItem);
    }

    public V putBack(final V oldItem) {
        return putBack(oldItem, 0, null);
    }


    /** accuracy determines the percentage of items which will be processNext().
     * this is a way to apply the forgetting process applied in putBack.
     */
    public void processNext(final float forgetCycles, float accuracy, final Memory m) {
        int conceptsToForget = Math.max(1, (int)Math.round(size() * accuracy));
        synchronized (forgetNext) {
            forgetNext.set(forgetCycles, m);
            for (int i = 0; i < conceptsToForget; i++) {
                UPDATE(forgetNext);
            }
        }
    }
    public void processNext(AtomicDouble forgetDurations, float accuracy, final Memory m) {
        float forgetCycles = m.param.cycles(forgetDurations);
        processNext(forgetCycles, accuracy, m);
    }

        /** x = takeOut(), then putBack(x)
         *  @forgetCycles forgetting time in cycles
         *  @return the variable that was updated, or null if none was taken out
         */
    public V processNext(final float forgetCycles, final Memory m) {
        V next;
        synchronized (forgetNext) {
            forgetNext.set(forgetCycles, m);
            UPDATE(forgetNext);
            next = forgetNext.getItem();
        }
        return next;
    }

    public double[] getPriorityDistribution(int bins) {
        return getPriorityDistribution(new double[bins]);
    }

    public double[] getPriorityDistribution(double[] x) {
        int bins = x.length;
        double total = 0;
        for (V e : values()) {
            float p = e.budget.getPriority();
            int b = bin(p, bins-1);
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
    
    /** slow, probably want to override in subclasses */
    public float getMinPriority() {
        float min = 1.0f;
        for (Item e : this) {
            float p = e.getPriority();
            if (p < min) min = p;
        }
        return min;            
    }
    
    /** slow, probably want to override in subclasses */
    public float getMaxPriority() {
        float max = 0.0f;
        for (Item e : this) {
            float p = e.getPriority();
            if (p > max) max = p;
        }
        return max;
    }

    /** for bags which maintain a separate name index from the items, more fine-granied access methods to avoid redundancy when possible */
    @Deprecated abstract public static class IndexedBag<E extends Item<K>,K> extends Bag<K, E> {

        public E TAKE(final K key) {
            return take(key, true);
        }


        /** registers the item */
        abstract protected void index(E value);
        /** unregisters it */
        abstract protected E unindex(K key);

        protected E unindex(E e) {
            return unindex(e.name());
        }

        abstract public E take(final K key, boolean unindex);

        public E TAKE(E value) { return TAKE(value.name()); }


        /**
         * Insert an item into the itemTable, and return the overflow
         *
         * @param newItem The Item to put in
         * @return null if nothing was displaced and if the item itself replaced itself,
         * or the The overflow Item if a different item had to be removed
         */
        abstract protected E addItem(final E newItem, boolean index);

        public E PUT(final E newItem) {
            return addItem(newItem, true);
        }


        /**
         * Add a new Item into the Bag via a BagSelector interface for lazy or cached instantiation of Bag items
         *
         * @return the item which was removed, which may be the input item if it could not be inserted; or null if nothing needed removed
         *
         * WARNING This indexing-avoiding version not completely working yet, so it is not used as of this commit
         */

        public E putInFast(BagSelector<K,E> selector) {

            E item = take( selector.name(), false );

            if (item != null) {
                item = (E)item.merge(selector);
                final E overflow = addItem(item, false);
                if (overflow == item) {
                    unindex(item.name());
                }
                return overflow;
            }
            else {
                item = selector.newItem();

                //compare by reference, sanity check
                if (item.name()!=selector.name())
                    throw new RuntimeException("Unrecognized selector and resulting new instance have different name()'s: item=" + item.name() + " selector=" + selector.name());

                // put the (new or merged) item into itemTable
                return PUT(item);
            }


        }
    }

}
