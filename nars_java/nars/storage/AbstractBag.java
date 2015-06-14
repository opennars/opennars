package nars.storage;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import nars.core.Parameters;
import nars.entity.Item;
import nars.inference.BudgetFunctions;


public abstract class AbstractBag<E extends Item> implements Iterable<E> {

    /**
     * relative threshold, only calculate once
     */
    private final float RELATIVE_THRESHOLD = Parameters.BAG_THRESHOLD;
    
    protected AtomicInteger forgettingRate = null;
    protected BagObserver<E> bagObserver = null;
    
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
    abstract public E get(final CharSequence key);
    
    abstract public Set<CharSequence> keySet();

    abstract public int getCapacity();

    abstract public float getMass();

    /**
     * Add a new Item into the Bag
     *
     * @param newItem The new Item
     * @param insertIntoNameTable  whether to insert the item into the nametable; allows avoiding this if it was never removed
     * @return Whether the new Item is added into the Bag
     */
    abstract public boolean putIn(final E newItem, boolean insertIntoNameTable);
    
    

    /**
     * Add a new Item into the Bag
     *
     * @param newItem The new Item
     * @return Whether the new Item is added into the Bag
     */
    public boolean putIn(final E newItem) {
        return putIn(newItem, true);
    }
    


    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    abstract public int size();

    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    abstract public E takeOut(boolean removeFromNameTable);
    abstract public E pickOut(final CharSequence key);    

    public void printAll() {
        for (CharSequence k : keySet()) {
            E v = get(k);
            System.out.println("  " + k + " " + v + " (" + v.getClass().getSimpleName() + ")" );
        }
    }
    
    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */    
    public E takeOut() {
        return takeOut(true);
    }
        

    /**
     * To start displaying the Bag in a BagWindow; {@link nars.gui.BagWindow}
     * implements interface {@link BagObserver};
     *
     * @param bagObserver BagObserver to set
     * @param title The title of the window
     */
    public void addBagObserver(BagObserver<E> bagObserver, String title) {
        this.bagObserver = bagObserver;
        bagObserver.post(toString());
        bagObserver.setTitle(title);
        bagObserver.setBag(this);
    }

    /**
     * Get the item decay rate, which differs in difference subclass, and can be
     * changed in run time by the user, so not a constant.
     *
     * @return The number of times for a decay factor to be fully applied, or -1 if forgetting is disabled in this bag
     */
    protected int forgetRate() {
        if (forgettingRate != null) {
            return forgettingRate.get();
        }
        return -1;
    }

    /**
     * Resume display
     */
    public void play() {
        if (bagObserver != null) {
            bagObserver.post(toString());
        }
    }

    /** called when an item is inserted or re-inserted */
    protected void reprocess(E x) {
        int r = forgetRate();
        if (r > 0) {
            BudgetFunctions.forget(x.budget, forgetRate(), RELATIVE_THRESHOLD);
        }
    }

    /**
     * Stop display
     */
    public void stop() {
        if (bagObserver != null) {
            bagObserver.stop();
        }
    }

    /**
     * Put an item back into the itemTable
     * <p>
     * The only place where the forgetting rate is applied
     *
     * @param oldItem The Item to put back
     * @return Whether the new Item is added into the Bag
     */
    public boolean putBack(final E oldItem, boolean insertIntoNameTable) {        
        reprocess(oldItem);
        return putIn(oldItem, insertIntoNameTable);
    }
    
    public boolean putBack(final E oldItem) {
        return putBack(oldItem, true);
    }
    
    /** x = takeOut(), then putBack(x) - without removing 'x' from nameTable 
     *  @return the variable that was updated, or null if none was taken out
     */
    public E processNext() {
        final E x = takeOut(false);
        if (x!=null) {
            //putBack():
            reprocess(x);
            
            boolean r = putIn(x, false);
            if (!r) {
                throw new RuntimeException("Bag.processNext");
            }
            return x;
        }
        else
            return null;
    }

    abstract public Collection<E> values();

    abstract public float getAveragePriority();
        
    /** iterates all items in descending priority */
    public abstract Iterator<E> iterator();

    /**
    *  Removes a key from the nameTable, but not from the bag itself.
    *  Use only after having tentatively removed an item with takeOut(false) to complete
    *  the removal operation.
    * 
    *  @return the previous value associated with <tt>key</tt>, or 
    *  <tt>null</tt> if there was no mapping for <tt>key</tt>.
    */
    abstract public E removeKey(CharSequence key);
    
}
