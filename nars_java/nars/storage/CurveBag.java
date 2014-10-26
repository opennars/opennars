package nars.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import nars.core.Memory;
import nars.entity.Item;
import nars.util.sort.FractalSortedItemList;
import nars.util.sort.SortedItemList;



public class CurveBag<E extends Item<K>, K> extends Bag<E,K> {
     
    final float MASS_EPSILON = 1e-5f;
    
    /**
     * mapping from key to item
     */
    public final Map<K, E> nameTable;
    
    /**
     * array of lists of items, for items on different level
     */
    public final SortedItemList<E> items;
    
    /**
     * defined in different bags
     */
    final int capacity;
    /**
     * current sum of occupied level
     */
    private float mass;
    
    /** whether items are removed by random sampling, or a continuous scanning */
    private final boolean randomRemoval;
    
    private final BagCurve curve;
    
    /** Rate of sampling index when in non-random "scanning" removal mode.  
     *  The position will be incremented/decremented by scanningRate/(numItems+1) per removal.
     *  Default scanning behavior is to start at 1.0 (highest priority) and decrement.
     *  When a value exceeds 0.0 or 1.0 it wraps to the opposite end (modulo).
     * 
     *  Valid values are: -1.0 <= x <= 1.0, x!=0      */
    final float scanningRate = -1.0f;
    
    /** current removal index x, between 0..1.0.  set automatically */
    private float x;
        
    public CurveBag(int capacity, boolean randomRemoval) {
        this(capacity, new FairPriorityProbabilityCurve(), randomRemoval);               
    }
    
    public CurveBag(int capacity, BagCurve curve, boolean randomRemoval) {
        this(capacity, curve, randomRemoval, new FractalSortedItemList<E>() 
                
                                /*if (capacity < 128)*/
                    //items = new ArraySortedItemList<>(capacity);
                /*else  {
                    //items = new FractalSortedItemList<>(capacity);
                    //items = new RedBlackSortedItemList<>(capacity);
                }*/
        );
    }
    
    public CurveBag(int capacity, BagCurve curve, boolean randomRemoval, SortedItemList<E> items) {
        super();
        this.capacity = capacity;
        this.randomRemoval = randomRemoval;        
        this.curve = curve;
        
        
        items.clear();
        items.setCapacity(capacity);
        this.items = items;
                
        if (randomRemoval)
            x = Memory.randomNumber.nextFloat();
        else
            x = 1.0f; //start a highest priority
        
        nameTable = new HashMap<>(capacity);
                
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
        
        int is = items.size();
        int in = nameTable.size();
        if (is!=in)
            throw new RuntimeException(this.getClass() + " inconsistent index: items=" + is + " names=" + in);
        
        return items.size();
    }

    /**
     * Get the average priority of Items
     *
     * @return The average priority of Items in the bag
     */
    @Override
    public float getAveragePriority() {
        final int s = size();
        if (s == 0) {
            return 0.01f;
        }
        float f = mass / size();
        if (f > 1f)
            return 1.0f;
        if (f < 0.01f)
            return 0.01f;
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
    public E get(final K key) {
        return nameTable.get(key);
    }

    @Override protected E putItem(final K name, final E item) {
        return nameTable.put(name, item);
    }



    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    @Override
    public E takeNext() {
        if (size()==0) return null; // empty bag                
        
        final E selected = removeItem( nextRemovalIndex() );
        if (removeKey(selected.name())!=selected)
            System.err.println("inconsistent item removal with name table removal: " + selected.name() + " -> " + selected);
        
        return selected;
    }

    @Override
    public E peekNext() {
        if (size()==0) return null; // empty bag                
                
        final E selected = items.get( nextRemovalIndex() );
        return selected;
    }
    
    
    /** distributor function */
    public int nextRemovalIndex() {      
        final float s = size();
        if (randomRemoval) {
            x = Memory.randomNumber.nextFloat();            
        }
        else {
            x += scanningRate * 1.0f / (1+s);
            if (x >= 1.0f)
                x -= 1.0f;
            if (x <= 0.0f)
                x += 1.0f;
        }
        
        float y = getFocus(x);
        if (y < 0) y = 0;
        if (y > 1.0f) y = 1f;
        
        int result = (int)Math.floor(y * s);            
        if (result == s) {
            throw new RuntimeException("Invalid removal index: " + x + " -> " + y + " " + result);
        }        
        
        return result;
    }
    
    /**
     * Defines the focus curve.  x is a proportion between 0 and 1 (inclusive).  x=0 represents low priority (bottom of bag), x=1.0 represents high priority
     * @param x
     * @return 
     */
    public float getFocus(final float x) {
        return (float)curve.y(x);
    }
    
//    public static long fastRound(final double d) {
//        if (d > 0) {
//            return (long) (d + 0.5d);
//        } else {
//            return (long) (d - 0.5d);
//        }
//    }
//    

    

    @Override public E removeKey(final K name) {
        return nameTable.remove(name);
    }



    @Override
    public float getMinPriority() {
        if (items.isEmpty()) return 0;
        return items.getFirst().getPriority();
    }
    @Override
    public float getMaxPriority() {
        if (items.isEmpty()) return 0;
        return items.getLast().getPriority();
    }
    
    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param newItem The Item to put in
     * @return The overflow Item, or null if nothing displaced
     */
    @Override protected E addItem(E newItem) {
        if (capacity == 500)
            System.out.println(" +A " + newItem + " " + items.size() + " " + nameTable.size() + " ");

        float newPriority = newItem.getPriority();        
        
        E oldItem = null;
        
        /*if (items.contains(newItem))
            return null;*/
        
        
        if (size() >= capacity) {      // the bag is full            
            if (newPriority < getMinPriority())
                return newItem;
            
            oldItem = removeItem(0);            
            removeKey(oldItem.name());
        }
        

        
        items.add(newItem);
        
        mass += (newItem.budget.getPriority());                  // increase total mass

        if (capacity == 500)
            System.out.println(" +B " + newItem + " " + items.size() + " " + nameTable.size() + " " + oldItem);

        return oldItem;
    }


    
    
    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    protected E removeItem(final int index) {
        //final E selected = (index == 0) ? items.removeFirst() : items.remove(index);
        final E selected = items.remove(index);
        mass -= selected.budget.getPriority();
       
        return selected;
    }

    /**
     * Remove an item from itemTable, then adjust mass
     *
     * @param oldItem The Item to be removed
     */ 
    @Override
    protected void removeItem(final E oldItem) {
        /*
        //A test for debugging to see if olditem and currentitem are ever different instances.
        final E currentItem = items.get(items.indexOf(oldItem));
        if (currentItem!=oldItem) {
            System.out.println("differing items: " + currentItem);
            System.out.println("  old: " + oldItem);
            throw new RuntimeException();
        }*/
        
        if (items.remove(oldItem)) {            
            mass -= oldItem.getPriority();
        }
        else
            throw new RuntimeException(this + " missing removeItem: " + oldItem);
    }



    @Override
    public float getMass() {
        if (mass < Float.MIN_VALUE)
            mass = 0;
        return mass+size();
    }
    

    @Override
    public int getCapacity() {
        return capacity;
    }


    @Override public String toString() {
        return super.toString() + "{" + items.getClass().getSimpleName() + "}";
    }
            

    @Override
    public Set<K> keySet() {
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

    /**
     * Defines the focus curve.  x is a proportion between 0 and 1 (inclusive).  x=0 represents low priority (bottom of bag), x=1.0 represents high priority
     * @param x input mappig value
     * @return
     */
    public static interface BagCurve {

        public double y(double x);
    }


    
    public static class CubicBagCurve implements nars.storage.CurveBag.BagCurve {

        @Override public final double y(final double x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-       
            return 1.0 - (x*x*x);
        }
        
    }
    
    /** Approximates priority -> probability fairness with an exponential curve */
    public static class FairPriorityProbabilityCurve implements nars.storage.CurveBag.BagCurve {

        @Override public final double y(final double x) {
            return 1 - Math.exp(-5 * x);
        }
        
    }
    
    
    
    public static class QuadraticBagCurve implements nars.storage.CurveBag.BagCurve {

        @Override public final double y(final double x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-       
            return 1 - (x*x);
        }
        
    }
    

        
    
}
