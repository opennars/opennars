package nars.storage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import nars.core.Memory;
import nars.entity.Item;
import nars.util.sort.IndexedTreeSet;


//WARNING NOT WORKING CURRENTLY
@Deprecated public class ContinuousBag2<E extends Item<K>,K> extends Bag<E,K> implements Comparator<E> {
     
    final float MASS_EPSILON = 1e-5f;
    
    /**
     * mapping from key to item
     */
    public final Map<K, E> nameTable;
    
    /**
     * array of lists of items, for items on different level
     */
    public final IndexedTreeSet<E> items;
    
    /**
     * defined in different bags
     */
    final int capacity;
    /**
     * current sum of occupied level
     */
    private float mass;
    
    /** whether items are removed by random sampling, or a continuous scanning */
    final protected boolean randomRemoval;
    
    /** Rate of sampling index when in non-random "scanning" removal mode.  
     *  The position will be incremented/decremented by scanningRate/(numItems+1) per removal.
     *  Default scanning behavior is to start at 1.0 (highest priority) and decrement.
     *  When a value exceeds 0.0 or 1.0 it wraps to the opposite end (modulo).
     * 
     *  Valid values are: -1.0 <= x <= 1.0, x!=0      */
    final protected float scanningRate = -1.0f;
    
    /** current removal index x, between 0..1.0.  set automatically */
    protected double x;
    private final BagCurve curve;
    

    /**
     * Defines the focus curve.  x is a proportion between 0 and 1 (inclusive).  x=0 represents low priority (bottom of bag), x=1.0 represents high priority
     * @param x input mappig value
     * @return 
     */
    public interface BagCurve {
        public double y(double x);        
    }
    
    public static class CubicBagCurve implements BagCurve {

        @Override public final double y(final double x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-       
            return 1.0 - (x*x*x);
        }
        
    }
    
    /** Approximates priority -> probability fairness with an exponential curve */
    public static class PriorityProbabilityApproximateCurve implements BagCurve {

        @Override public final double y(final double x) {
            return 1 - Math.exp(-5 * x);
        }
        
    }
    
    
    
    public static class QuadraticBagCurve implements BagCurve {

        @Override public final double y(final double x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-       
            return 1 - (x*x);
        }
        
    }
    
    
    public ContinuousBag2(int capacity, BagCurve curve, boolean randomRemoval) {
        super();
        this.capacity = capacity;
        this.randomRemoval = randomRemoval;        
        this.curve = curve;
        
        if (randomRemoval)
            x = Memory.randomNumber.nextFloat();
        else
            x = 1.0f; //start a highest priority
        
        nameTable = new HashMap<>(capacity);        //nameTable = new FastMap<>();
        
        items = new IndexedTreeSet<>(this);
                            
        this.mass = 0;
    }

    @Override public final int compare(final E ex, final E ey) {
        float y = ey.budget.getPriority();
        float x = ex.budget.getPriority();
        
        return (x < y) ? -1 : ((x == y) ? (ex.compareTo(ey)) : 1);                   
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

    @Override protected E namePut(final K name, final E item) {
        return nameTable.put(name, item);
    }

    @Override protected E nameRemove(final K name) {
        return nameTable.remove(name);
    }


    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    @Override
    public E takeOut() {
        if (size()==0) return null; // empty bag                
        
        final E selected = takeOutIndex( nextRemovalIndex() );
        
        return selected;
    }
    

    @Override
    public E peekNext() {
        if (size()==0) return null; // empty bag                
                
        final E selected = items.exact( nextRemovalIndex() );
        return selected;
    }
    
    
    /** distributor function */
    public int nextRemovalIndex() {      
        //TODO use common AbstractContinuousBag because this should be identical with ContinuousBag's method
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
        
        float y = (float) curve.y(x);
        if (y < 0) y = 0;
        if (y > 1.0f) y = 1f;
        
        int result = (int)Math.round(y * (s-1));            
        if (result == s) {
            throw new RuntimeException("Invalid removal index: " + x + " -> " + y + " " + result);
        }        
        
        return result;
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
    public E pickOut(final K key) {
        final E picked = nameTable.get(key);
        if (picked != null) {
            outOfBase(picked);
            nameTable.remove(key);
        }
        return picked;
    }




    public float getMinPriority() {
        if (items.isEmpty()) return 0;
        return items.first().getPriority();
    }
    public float getMaxPriority() {
        if (items.isEmpty()) return 0;
        return items.last().getPriority();
    }
    
    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param newItem The Item to put in
     * @return The overflow Item
     */
    @Override protected E intoBase(E newItem) {
        float newPriority = newItem.getPriority();
        
        E oldItem = null;        
                
        if (size() >= capacity) {      // the bag is full            
            if (newPriority < getMinPriority())
                return newItem;
            
            oldItem = takeOutIndex(0);            
        }
        
        items.add(newItem);
        
        mass += (newItem.budget.getPriority());                  // increase total mass
        return oldItem;
    }

    
    
    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    private E takeOutIndex(final int index) {        
        E e = items.exact(index);
        boolean removed = items.remove(e);
                
        nameTable.remove(e.name());
        
        mass -= e.budget.getPriority();
        
        return e;
    }

    /**
     * Remove an item from itemTable, then adjust mass
     *
     * @param oldItem The Item to be removed
     */
    @Override
    protected void outOfBase(final E oldItem) {
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


    @Override
    public String toString() {
        return size() + " size, " + getMass() + " mass, items: " + items.toString();
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

    
}
