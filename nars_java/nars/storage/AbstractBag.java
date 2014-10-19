package nars.storage;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.Item;


public abstract class AbstractBag<E extends Item<K>,K> implements Iterable<E> {
    
    
    
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
    

//    /**
//     * To start displaying the Bag in a BagWindow; {@link nars.gui.BagWindow}
//     * implements interface {@link BagObserver};
//     *
//     * @param bagObserver BagObserver to set
//     * @param title The title of the window
//     */
//    public void addBagObserver(BagObserver<E> bagObserver, String title) {
//        this.bagObserver = bagObserver;
//        bagObserver.post(toString());
//        bagObserver.setTitle(title);
//        bagObserver.setBag(this);
//    }

    
//    /**
//     * Resume display
//     */
//    public void play() {
//        if (bagObserver != null) {
//            bagObserver.post(toString());
//        }
//    }


//    /**
//     * Stop display
//     */
//    public void stop() {
//        if (bagObserver != null) {
//            bagObserver.stop();
//        }
//    }


    
//    /** called when an item is inserted or re-inserted */
//    protected void forgetExperimental(final E x) {
//        float forgetCycles = forgetCycles();
//        if (forgetCycles > 0) {
//            
//            
//            //increase the forgetting time (lower rate) by the empty space of the bag to keep the 
//            //actual forgetting rate constant over time, regardless of bag size vs. capacity.
//            final float size = size();
//            float initialPriority = x.budget.getPriority();
//
//            
//            
//            BudgetFunctions.forget(x.budget, forgetCycles, RELATIVE_THRESHOLD);
//
//            
//            
////            //use LERP to give the priority a momentum to simulate slower forgetting.
////            //the rate is proportional to the empty bag space divided by the priority
////            //this is because higher priority concepts will be activated more frequently
////            //their forgetting should be more frequent
////            float emptyBagFactor = (1f + size) / (1f + getCapacity()) - initialPriority;
////            if (emptyBagFactor > 0)
////                x.budget.lerpPriority(initialPriority, emptyBagFactor);
////            
////            if (x instanceof Concept)
////                System.out.println(x.toString() + " " + size + "|" + getCapacity() + ": " + forgetCycles + " * " + initialPriority + " -> " +  x.budget.getPriority() + " " + emptyBagFactor);
//        }
//    }


    abstract public Collection<E> values();

    abstract public float getAveragePriority();
        
    /** iterates all items in descending priority */
    @Override
    public abstract Iterator<E> iterator();


    
    /**
     * Put an item back into the itemTable
     * <p>
     * The only place where the forgetting rate is applied
     *
     * @param oldItem The Item to put back
     * @return Whether the new Item is added into the Bag
     */    
    public final boolean putBack(final E oldItem, float forgetCycles, Memory m) {
        float relativeThreshold = Parameters.BAG_THRESHOLD;
        m.forget(oldItem, forgetCycles, relativeThreshold);
        return putIn(oldItem);
    }

    
    /** x = takeOut(), then putBack(x)
     *  @forgetCycles forgetting time in cycles
     *  @return the variable that was updated, or null if none was taken out
     */
    synchronized public E processNext(float forgetCycles, float relativeThreshold, Memory m) {
        final E x = takeOut();
        if (x!=null) {
            
            m.forget(x, forgetCycles, relativeThreshold);
                        
            boolean r = putIn(x);
            if (!r) {
                throw new RuntimeException("Bag.processNext");
            }
            return x;
        }
        else {
            return null;
        }
    }
    

    
    public double[] getPriorityDistribution(int bins) {
        double[] x = new double[bins];
        double total = 0;
        for (E e : values()) {
            float p = e.budget.getPriority();
            int b = (int)Math.floor(p * bins);
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
