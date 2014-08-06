package nars.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import nars.core.Parameters;
import nars.entity.Item;
import nars.entity.ShortFloat;
import nars.storage.AbstractBag;
import nars.storage.Memory;



public class ContinuousBag<E extends Item> extends AbstractBag<E> {
 

    /**
     * hashtable load factor
     */
    public static final float LOAD_FACTOR = Parameters.LOAD_FACTOR;       //
    

    
    
    /**
     * mapping from key to item
     */
    public final Map<String, E> nameTable;
    
    /**
     * array of lists of items, for items on different level
     */
    public final PrioritySortedItemList<E> items;
    
    /**
     * defined in different bags
     */
    final int capacity;
    /**
     * current sum of occupied level
     */
    private int mass;



    public ContinuousBag(int capacity, int forgetRate) {
        this(capacity, new AtomicInteger(forgetRate));
    }
    
    public ContinuousBag(int capacity, AtomicInteger forgetRate) {
        super();
        this.capacity = capacity;
        nameTable = new HashMap<>((int) (capacity / LOAD_FACTOR), LOAD_FACTOR);
        //nameTable = new FastMap<>();//(int) (capacity / LOAD_FACTOR), LOAD_FACTOR);
        items = new PrioritySortedItemList<E>(capacity);
        this.forgettingRate = forgetRate;
        this.mass = 0;
    }
    

    @Override
    public final void clear() {
        items.clear();
        nameTable.clear();
        mass = 0;
    }

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    @Override
    public int size() {
        return nameTable.size();
    }

    /**
     * Get the average priority of Items
     *
     * @return The average priority of Items in the bag
     */
    public float getAveragePriority() {
        if (size() == 0) {
            return 0.01f;
        }
        float f = (float) mass / capacity;
        if (f > 1) {
            return 1.0f;
        }
        return f;
    }

    /**
     * Check if an item is in the bag
     *
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    @Override
    public boolean contains(final E it) {
        return nameTable.containsValue(it);
    }

    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    @Override
    public E get(final String key) {
        return nameTable.get(key);
    }

    /**
     * Add a new Item into the Bag
     *
     * @param newItem The new Item
     * @return Whether the new Item is added into the Bag
     */
    @Override
    public boolean putIn(final E newItem, boolean nameTableInsert) {
        //TODO this is identical with Bag, should merge?
        if (nameTableInsert) {
            final String newKey = newItem.getKey();                        
            final E oldItem = nameTable.put(newKey, newItem);
            if (oldItem != null) {                  // merge duplications
                outOfBase(oldItem);
                newItem.merge(oldItem);
            }
        }
        
        final E overflowItem = intoBase(newItem);  // put the (new or merged) item into itemTable
        if (overflowItem != null) {             // remove overflow
            final String overflowKey = overflowItem.getKey();
            nameTable.remove(overflowKey);
            return (overflowItem != newItem);
        } else {
            return true;
        }
    }


    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    @Override
    public E takeOut(boolean removeFromNameTable) {
        int c = size();
                
        if (c == 0) return null; // empty bag                
        
        final E selected = takeOutIndex( nextRemovalIndex() );
        
        if (removeFromNameTable) {
            nameTable.remove(selected.getKey());
        }
        return selected;
    }
    
    /** distributor function */
    public int nextRemovalIndex() {
        // 1.0 - (random())^3 :: a function which has domain and range between 0..1.0 but
        // will result in values above 0.5 more often than not.  see the curve:
        //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIoMS14XjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-

        double p = Memory.randomNumber.nextDouble();
        p = 1-(p*p*p);
        return (int)fastRound(p * (size()-1));
    }
    
    public static long fastRound(final double d) {
        if (d > 0) {
            return (long) (d + 0.5d);
        } else {
            return (long) (d - 0.5d);
        }
    }

    
    /**
     * Pick an item by key, then remove it from the bag
     *
     * @param key The given key
     * @return The Item with the key
     */
    @Override
    public E pickOut(final String key) {
        final E picked = nameTable.get(key);
        if (picked != null) {
            outOfBase(picked);
            nameTable.remove(key);
        }
        return picked;
    }



    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param newItem The Item to put in
     * @return The overflow Item
     */
    private E intoBase(E newItem) {
        E oldItem = null;
        
        if (size() > capacity) {      // the bag is full            
            oldItem = takeOutIndex(0);
        }
        
        items.add(newItem);
        
        mass += (newItem.budget.getPriorityShort());                  // increase total mass
        return oldItem;		// TODO return null is a bad smell
    }


    
    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    private E takeOutIndex(final int index) {
        //final E selected = (index == 0) ? items.removeFirst() : items.remove(index);
        final E selected = items.remove(index);
        mass -= selected.budget.getPriorityShort();
        return selected;
    }

    /**
     * Remove an item from itemTable, then adjust mass
     *
     * @param oldItem The Item to be removed
     */
    protected void outOfBase(final E oldItem) {
        if (items.remove(oldItem)) {
            //mass could be incorrect if priority changes while it was inserted
            mass -= (oldItem.getPriority());            
        }
    }



    @Override
    public float getMass() {
        if (mass < ShortFloat.MIN_VALUE)
            mass = 0;
        return mass;
    }
    

    @Override
    public int getCapacity() {
        return capacity;
    }


    @Override
    public String toString() {
        return size() + " size, " + getMass() + " mass, items: " + items.toString();
    }

    @Override
    public Set<String> keySet() {
        return nameTable.keySet();
    }

    @Override
    public Collection<E> values() {
        return nameTable.values();
    }


    
    
    
}
