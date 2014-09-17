package nars.storage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import nars.core.Memory;
import nars.entity.Item;
import nars.util.sort.IndexedTreeSet;



public class ContinuousBag2<E extends Item> extends AbstractBag<E> implements Comparator<E> {
     
    final float MASS_EPSILON = 1e-5f;
    
    /**
     * mapping from key to item
     */
    public final Map<CharSequence, E> nameTable;
    
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
    private final boolean randomRemoval;
    
    /** Rate of sampling index when in non-random "scanning" removal mode.  
     *  The position will be incremented/decremented by scanningRate/(numItems+1) per removal.
     *  Default scanning behavior is to start at 1.0 (highest priority) and decrement.
     *  When a value exceeds 0.0 or 1.0 it wraps to the opposite end (modulo).
     * 
     *  Valid values are: -1.0 <= x <= 1.0, x!=0      */
    final float scanningRate = -1.0f;
    
    /** current removal index x, between 0..1.0.  set automatically */
    private double x;
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

        @Override
        public final double y(final double x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-        
            return (x*x*x);
        }
        
    }
    
    /** estimates probability curve of DefaultBag */
    public static class DefaultBagCurve implements BagCurve {

        final static double a = 4.61484E-8;
        final static double b = 4.34690E-6;
        final static double c = 0.00025931;
        
        @Override final public double y(double x) {
            //probability curve
            //return a*x*x*x - b*x*x + c*x;
            
            double y = 0;
            
            y += c*x; //^1
            x*=x; 
            y += b*x; //^2
            x*=x; 
            y += a*x; //^3
            
            //multiply by x for each term to convert mapping to a probability:
            y*=x;
            
            return y;
        }
        
    }
    
    
    
    public ContinuousBag2(int capacity, int forgetRate, BagCurve curve, boolean randomRemoval) {
        this(capacity, new AtomicInteger(forgetRate), curve, randomRemoval);
    }
    
    public ContinuousBag2(int capacity, AtomicInteger forgetRate, BagCurve curve, boolean randomRemoval) {
        super();
        this.capacity = capacity;
        this.randomRemoval = randomRemoval;        
        this.curve = curve;
        
        if (randomRemoval)
            x = Memory.randomNumber.nextFloat();
        else
            x = 1.0f; //start a highest priority
        
        nameTable = new HashMap<>(capacity);        //nameTable = new FastMap<>();
        
        items = new IndexedTreeSet<E>(this);
            
        
        this.forgettingRate = forgetRate;
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
    public E get(final CharSequence key) {
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
            final CharSequence newKey = newItem.getKey();                        
            final E oldItem = nameTable.put(newKey, newItem);
            if (oldItem != null) {                  // merge duplications
                outOfBase(oldItem);
                newItem.merge(oldItem);
            }
        }
        
        final E overflowItem = intoBase(newItem);  // put the (new or merged) item into itemTable
        if (overflowItem != null) {             // remove overflow
            final CharSequence overflowKey = overflowItem.getKey();
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
        if (size()==0) return null; // empty bag                
        
        final E selected = takeOutIndex( nextRemovalIndex(), removeFromNameTable );
        
        return selected;
    }
    
    
    /** distributor function */
    public int nextRemovalIndex() {      
        final int s = size();
        if (randomRemoval) {
            //uniform random distribution on 0..1.0
            x = Memory.randomNumber.nextFloat();            
        }
        else {
            x += scanningRate * 1.0f / (1+s);
            if (x >= 1.0f)
                x = x - 1.0f;
            if (x <= 0.0f)
                x = x + 1.0f;
        }
        
        double y = curve.y(x);
        
        int result = (int)fastRound((1.0-y) * (s-1));            
        if (result == s) {
            throw new RuntimeException("Invalid removal index: " + x + " -> " + y);
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
    public E pickOut(final CharSequence key) {
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
    private E intoBase(final E newItem) {
        E oldItem = null;
        
        if (size() >= capacity) {      // the bag is full            
            oldItem = takeOutIndex(0, true);
        }
        
        items.add(newItem);
        
        mass += (newItem.budget.getPriority());                  // increase total mass
        return oldItem;		// TODO return null is a bad smell
    }


    
    
    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    private E takeOutIndex(final int index, final boolean removeFromNameTable) {        
        E e = items.exact(index);
        boolean removed = items.remove(e);
        
        if (removeFromNameTable)
            nameTable.remove(e.getKey());
        
        addToMass(-(e.budget.getPriority()));
        
        return e;
    }

    /**
     * Remove an item from itemTable, then adjust mass
     *
     * @param oldItem The Item to be removed
     */
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
            addToMass(-(oldItem.getPriority()));
        }
    }

    protected void addToMass(float delta) {
        mass += delta;
        if (mass < MASS_EPSILON)  mass = 0;
        if (mass > -MASS_EPSILON) mass = 0;
        if (mass < 0)
            throw new RuntimeException("Mass < 0: mass=" + mass +", items=" + size());        
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
    public Set<CharSequence> keySet() {
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

    @Override public E removeKey(final CharSequence key) {
        return nameTable.remove(key);
    }

    
    
    
}
