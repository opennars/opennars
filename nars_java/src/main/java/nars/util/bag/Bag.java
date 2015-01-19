package nars.util.bag;

import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.Item;

import java.util.Iterator;
import java.util.Set;


public abstract class Bag<E extends Item<K>,K> implements Iterable<E> {


    /** for bags which maintain a separate name index from the items, more fine-granied access methods to avoid redundancy when possible */
    abstract public static class IndexedBag<E extends Item<K>,K> extends Bag<E,K> {

        public E take(final K key) {
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

        public E take(E value) { return take(value.name()); }


        /**
         * Insert an item into the itemTable, and return the overflow
         *
         * @param newItem The Item to put in
         * @return null if nothing was displaced and if the item itself replaced itself,
         * or the The overflow Item if a different item had to be removed
         */
        abstract protected E addItem(final E newItem, boolean index);

        protected E addItem(final E newItem) {
            return addItem(newItem, true);
        }


        /**
         * Add a new Item into the Bag via a BagSelector interface for lazy or cached instantiation of Bag items
         *
         * @return the item which was removed, which may be the input item if it could not be inserted; or null if nothing needed removed
         *
         * WARNING This indexing-avoiding version not completely working yet, so it is not used as of this commit
         */

        public synchronized E putInFast(BagSelector<K,E> selector) {

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
                item = selector.newInstance();

                //compare by reference, sanity check
                if (item.name()!=selector.name())
                    throw new RuntimeException("Unrecognized selector and resulting new instance have different name()'s: item=" + item.name() + " selector=" + selector.name());

                // put the (new or merged) item into itemTable
                return addItem(item);
            }


        }
    }

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
    public boolean contains(final E it) {
        E exist = get(it.name());
        return exist.equals(it);
    }
    
    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    abstract public E get(final K key);
    
    abstract public Set<K> keySet();

    abstract public int getCapacity();

    abstract public float getMass();

    /**
     * Choose an Item according to distribution policy and take it out of the Bag
     * @return The selected Item, or null if this bag is empty
     */
    abstract public E takeNext();
    

    /** gets the next value without removing changing it or removing it from any index.  however
     the bag is cycled so that subsequent elements are different. */    
    abstract public E peekNext();

    abstract protected E addItem(E n);

    public boolean isEmpty() {
        return size() == 0;
    }

    abstract public E take(K key);

    public E take(E item) {
        return take(item.name());
    }

    /**
     * Add a new Item into the Bag
     *
     * @param newItem The new Item
     * @return the item which was removed, which may be the input item if it could not be inserted; or null if nothing needed removed
     */
    public E putIn(E newItem) {
                
        final K newKey = newItem.name();
        
        final E existingItemWithSameKey = take(newKey);

        E item;
        if (existingItemWithSameKey != null) {            
            item = (E)existingItemWithSameKey.merge(newItem);
        }
        else {
            item = newItem;
        }
        
        // put the (new or merged) item into itemTable        
        final E overflowItem = addItem(item);
        
        
        if (overflowItem!=null) {
            return overflowItem;
        }            
        else {
            return null;
        }
        
    }



    /**
     * Add a new Item into the Bag via a BagSelector interface for lazy or cached instantiation of Bag items
     *
     * @return the item which was removed, which may be the input item if it could not be inserted; or null if nothing needed removed
     */
    public synchronized E putIn(BagSelector<K,E> selector) {

        E item = take( selector.name() );

        if (item != null) {
            item = (E)item.merge(selector);
            final E overflow = addItem(item );
            return overflow;
        }
        else {
            item = selector.newInstance();

            // put the (new or merged) item into itemTable
            return addItem(item);
        }

    }



    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    public abstract int size();
    
    
    public void printAll() {
        Iterator<E> d = iterator();
        while (d.hasNext()) {
            System.out.println("  " + d.next() + "\n" );
        }
    }
    
    abstract public Iterable<E> values();

    public abstract float getAveragePriority();

    public float getTotalPriority() {
        int size = size();
        if (size == 0) return 0;
        return getAveragePriority() * size();
    }

    /** iterates all items in (approximately) descending priority */
    @Override public abstract Iterator<E> iterator();
    
    /** allows adjusting forgetting rate in subclasses */    
    public float getForgetCycles(final float baseForgetCycles, final E item) {
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
    public E putBack(final E oldItem, final float forgetCycles, final Memory m) {
        float relativeThreshold = Parameters.FORGET_QUALITY_RELATIVE;
        m.forget(oldItem, getForgetCycles(forgetCycles, oldItem), relativeThreshold);
        return putIn(oldItem);
    }
    
    
    /** x = takeOut(), then putBack(x)
     *  @forgetCycles forgetting time in cycles
     *  @return the variable that was updated, or null if none was taken out
     */
    public E processNext(final float forgetCycles, final Memory m) {
                
        final E x = takeNext();
        if (x == null)
            return null;
        
        E r = putBack(x, forgetCycles, m);
        if (r!=null) {
            throw new RuntimeException("Bag.processNext should always be able to re-insert item: " + r);
        }
        return x;
    }

    public double[] getPriorityDistribution(int bins) {
        return getPriorityDistribution(new double[bins]);
    }

    public double[] getPriorityDistribution(double[] x) {
        int bins = x.length;
        double total = 0;
        for (E e : values()) {
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

    
}
