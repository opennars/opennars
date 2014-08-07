package nars.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import nars.entity.Item;
import nars.entity.ShortFloat;
import nars.storage.AbstractBag;
import nars.storage.Memory;



public class ContinuousBag<E extends Item> extends AbstractBag<E> {
     
    
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
    
    /** whether items are removed by random sampling, or a continuous scanning */
    private boolean randomRemoval;
    
    /** Rate of sampling index when in non-random "scanning" removal mode.  
     *  The position will be incremented/decremented by scanningRate/(numItems+1) per removal.
     *  Default scanning behavior is to start at 1.0 (highest priority) and decrement.
     *  When a value exceeds 0.0 or 1.0 it wraps to the opposite end (modulo).
     * 
     *  Valid values are: -1.0 <= x <= 1.0, x!=0      */
    final float scanningRate = -1.0f;
    
    /** current removal index x, between 0..1.0.  set automatically */
    private float x;

    public ContinuousBag(int capacity, int forgetRate, boolean randomRemoval) {
        this(capacity, new AtomicInteger(forgetRate), randomRemoval);
    }
    
    public ContinuousBag(int capacity, AtomicInteger forgetRate, boolean randomRemoval) {
        super();
        this.capacity = capacity;
        this.randomRemoval = randomRemoval;        
        
        if (randomRemoval)
            x = Memory.randomNumber.nextFloat();
        else
            x = 1.0f; //start a highest priority
        
        nameTable = new HashMap<>(capacity);        //nameTable = new FastMap<>();
        
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
        int size = size();
        if (size == 0)
            throw new RuntimeException("No removal index for empty " + this);
                
        if (randomRemoval) {
            x = getFocus(Memory.randomNumber.nextFloat());            
        }
        else {
            x += scanningRate * 1.0f / (1+size);
            if (x >= 1.0f)
                x = x - 1.0f;
            if (x <= 0.0f)
                x = x + 1.0f;
        }
        
        float y = getFocus(x);
        
        int result = (int)fastRound(y * (size()-1));            
        if (result == capacity) {
            throw new RuntimeException("Invalid removal index: " + x + " -> " + y);
        }
        
        return result;
    }
    
    /**
     * Defines the focus curve.  x is a proportion between 0 and 1 (inclusive).  x=0 represents low priority (bottom of bag), x=1.0 represents high priority
     * @param x
     * @return 
     */
    public float getFocus(float x) {
        //1.0 - ((1.0-x)^2)
        // a function which has domain and range between 0..1.0 but
        //   will result in values above 0.5 more often than not.  see the curve:        
        //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-
        float oneMinusX = 1.0f - x;
        return 1.0f - (oneMinusX*oneMinusX);
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

    @Override
    public Iterator<E> iterator() {        
        return items.descendingIterator();
    }

    @Override public E removeKey(final String key) {
        return nameTable.remove(key);
    }

    
    
    
}
