package nars.storage;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.Item;


public abstract class Bag<E extends Item<K>,K> implements Iterable<E> {
    
    //protected BagObserver<E> bagObserver = null;
    
    abstract public void clear();

    /**
     * Check if an item is in the bag
     *
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    abstract public boolean contains(final E it);

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
     * Add a new Item into the Bag
     *
     * @param newItem The new Item
     * @return Whether the new Item is added into the Bag
     */
    abstract public boolean putIn(final E newItem);

    

    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    abstract public E takeOut();    

    /** gets the next value without removing changing it or removing it from any index */
    abstract public E peekNext();
    
    abstract public E pickOut(final K key);    

    
    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    abstract public int size();

    
    
    public void printAll() {
        for (K k : keySet()) {
            E v = get(k);
            System.out.println("  " + k + " " + v + " (" + v.getClass().getSimpleName() + ")" );
        }
    }
    

    abstract public Collection<E> values();

    abstract public float getAveragePriority();
        
    /** iterates all items in descending priority */
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
     * @return Whether the new Item is added into the Bag
     */    
    public boolean putBack(final E oldItem, final float forgetCycles, final Memory m) {
        float relativeThreshold = Parameters.BAG_THRESHOLD;
        m.forget(oldItem, getForgetCycles(forgetCycles, oldItem), relativeThreshold);
        return putIn(oldItem);
    }

    
    /** x = takeOut(), then putBack(x)
     *  @forgetCycles forgetting time in cycles
     *  @return the variable that was updated, or null if none was taken out
     */
    synchronized public E processNext(final float forgetCycles, final Memory m) {
        final E x = takeOut();
        if (x!=null) {
            
            boolean r = putBack(x, forgetCycles, m);
            if (!r) {
                throw new RuntimeException("Bag.processNext");
            }
            return x;
        }
        else {
            return null;
        }
    }
    

    public static final int bin(final float x, final int bins) {
        int i = (int)Math.floor((x + 0.5f/bins) * bins);
        return i;
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


    
}
