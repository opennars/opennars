package nars.util.bag;

import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.Item;
import nars.util.sort.ArraySortedIndex;
import nars.util.sort.SortedIndex;

import java.util.*;


public class CurveBag<E extends Item<K>, K> extends Bag.IndexedBag<E, K> {

    final float MASS_EPSILON = 1e-5f;

    /**
     * mapping from key to item
     */
    public final CurveMap nameTable;

    /**
     * array of lists of items, for items on different level
     */
    public final SortedIndex<E> items;

    /**
     * defined in different bags
     */
    final int capacity;
    /**
     * current sum of occupied level
     */
    private float mass;

    /**
     * whether items are removed by random sampling, or a continuous scanning
     */
    private final boolean randomRemoval;

    private final BagCurve curve;

    /**
     * Rate of sampling index when in non-random "scanning" removal mode.
     * The position will be incremented/decremented by scanningRate/(numItems+1) per removal.
     * Default scanning behavior is to start at 1.0 (highest priority) and decrement.
     * When a value exceeds 0.0 or 1.0 it wraps to the opposite end (modulo).
     * <p>
     * Valid values are: -1.0 <= x <= 1.0, x!=0
     */
    final float scanningRate = -1.0f;

    /**
     * current removal index x, between 0..1.0.  set automatically
     */
    private float x;


    public static <E extends Item> SortedIndex<E> getIndex(int capacity) {
        //if (capacity < 50)            
        return new ArraySortedIndex<E>(capacity);
        /*else
            return new FractalSortedItemList<E>();        */
    }

    public CurveBag(int capacity, boolean randomRemoval) {
        this(capacity, new FairPriorityProbabilityCurve(), randomRemoval);
    }

    public CurveBag(int capacity, BagCurve curve, boolean randomRemoval) {
        this(capacity, curve, randomRemoval,
                getIndex(capacity)                
                
                                /*if (capacity < 128)*/
                //items = new ArraySortedItemList<>(capacity);
                /*else  {
                    //items = new FractalSortedItemList<>(capacity);
                    //items = new RedBlackSortedItemList<>(capacity);
                }*/
        );
    }

    public class CurveMap extends HashMap<K, E> {

        public CurveMap(int initialCapacity) {
            super(initialCapacity * 1 + 1);
        }

        @Override
        public E put(final K key, final E value) {

            E removed;

            synchronized (nameTable) {

                removed = putKey(key, value);
                if (removed != null) {
                    removeItem(removed);
                }

                addItem(value);
            }

            return removed;
        }

        /**
         * put key in index, do not add value
         */
        public E putKey(final K key, final E value) {
            return super.put(key, value);
        }

        /**
         * remove key only, not from items
         */
        public E removeKey(final K key) {
            return super.remove(key);
        }

        public E removeItem(K name) {
            synchronized (nameTable) {
                E item = nameTable.get(name);
                if (item == null) return null;
                return removeItem(item);
            }
        }

        public E removeItem(final E removed) {
            if (items.remove(removed)) {
                mass -= removed.budget.getPriority();
                return removed;
            }
            return null;
        }

        public void addItem(final E removed) {
            items.add(removed);
        }

        @Override
        public E remove(final Object key) {

            E e;

            synchronized (nameTable) {

                e = removeKey((K) key);
                if (e != null) {
                    removeItem(e);
                }

            }
            return e;
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new RuntimeException("Not implemented");
        }


    }

    public CurveBag(int capacity, BagCurve curve, boolean randomRemoval, SortedIndex<E> items) {
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

        nameTable = new CurveMap(capacity);

        this.mass = 0;
    }


    @Override
    public final void clear() {
        synchronized (nameTable) {
            items.clear();
            nameTable.clear();
            mass = 0;
        }
    }

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    @Override
    public int size() {

        int in = nameTable.size();

        if (Parameters.DEBUG) {
            int is = items.size();

            //test for a discrepency of +1/-1 difference between name and items
            if ((is - in > 2) || (is - in < -2)) {
                System.err.println(this.getClass() + " inconsistent index: items=" + is + " names=" + in);
                /*System.out.println(nameTable);
                System.out.println(items);
                if (is > in) {
                    List<E> e = new ArrayList(items);
                    for (E f : nameTable.values())
                        e.remove(f);
                    System.out.println("difference: " + e);
                }*/
                throw new RuntimeException(this.getClass() + " inconsistent index: items=" + is + " names=" + in);
            }
        }

        return in;
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
        float f = mass / s;
        if (f > 1f)
            return 1.0f;
        if (f < 0.01f)
            return 0.01f;
        return f;
    }

    @Override
    protected void index(E value) {
        /*E oldValue = */
        nameTable.putKey(value.name(), value);
    }

    @Override
    protected E unindex(K name) {
        E removed = nameTable.removeKey(name);
        return removed;
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
    public E GET(final K key) {
        return nameTable.get(key);
    }


    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    @Override
    public E TAKENEXT() {

        if (size() == 0) return null; // empty bag
        return removeItem(nextRemovalIndex());

    }


    @Override
    public E PEEKNEXT() {

        if (size() == 0) return null; // empty bag
        return items.get(nextRemovalIndex());

    }


    /**
     * distributor function
     */
    public int nextRemovalIndex() {
        final float s = items.size();
        if (randomRemoval) {
            x = Memory.randomNumber.nextFloat();
        } else {
            x += scanningRate * 1.0f / (1 + s);
            if (x >= 1.0f)
                x -= 1.0f;
            if (x <= 0.0f)
                x += 1.0f;
        }

        float y = getFocus(x);
        if (y < 0) y = 0;
        if (y > 1.0f) y = 1f;

        int result = (int) Math.floor(y * s);
        if (result == s) {
            //throw new RuntimeException("Invalid removal index: " + x + " -> " + y + " " + result);
            result = (int) (s - 1);
        }

        return result;
    }

    /**
     * Defines the focus curve.  x is a proportion between 0 and 1 (inclusive).  x=0 represents low priority (bottom of bag), x=1.0 represents high priority
     *
     * @param x
     * @return
     */
    public float getFocus(final float x) {
        return (float) curve.y(x);
    }

//    public static long fastRound(final double d) {
//        if (d > 0) {
//            return (long) (d + 0.5d);
//        } else {
//            return (long) (d - 0.5d);
//        }
//    }
//    


    @Override
    public E take(final K name, boolean unindex) {
        return unindex ? nameTable.remove(name) : nameTable.removeItem(name);
    }

    public synchronized E PUT(BagSelector<K, E> selector) {
        return super.putInFast(selector);
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
     * @param i The Item to put in
     * @return The overflow Item, or null if nothing displaced
     */
    @Override
    protected E addItem(E i, boolean index) {

        synchronized (nameTable) {
            float newPriority = i.getPriority();

            E oldItem = null;

            if (nameTable.size() >= capacity) {
                // the bag is full

                // this item is below the bag's already minimum item
                if (newPriority < getMinPriority())
                    return i;

                oldItem = removeItem(0);
            }

            if (index)
                nameTable.put(i.name(), i);
            else
                nameTable.addItem(i);

            mass += (i.budget.getPriority());                  // increase total mass

            return oldItem;
        }


    }


    protected synchronized E removeItem2(final int index) {

        final E selected;

        selected = items.remove(index);
        if (selected != null) {
            E removed = nameTable.removeKey(selected.name());

            if (removed == null)
                throw new RuntimeException(this + " inconsistent index: items contained " + selected + " but had no key referencing it");

            //should be the same object instance
            if ((removed != null) && (removed != selected)) {
                throw new RuntimeException(this + " inconsistent index: items contained " + selected + " and index referenced " + removed + " + ");
            }
            mass -= selected.budget.getPriority();
        } else {
            throw new RuntimeException(this + " items array returned null item at index " + index);
        }

        return selected;
    }


    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    protected E removeItem(final int index) {

        final E selected;

        synchronized (nameTable) {

            selected = items.remove(index);
            if (selected == null)
                throw new RuntimeException(this + " inconsistent index: items contained " + selected + " but had no key referencing it");

            //should be the same object instance
            if (selected != null) {
                nameTable.removeKey(selected.name());
                mass -= selected.budget.getPriority();
            }
        }

        return selected;
    }


    @Override
    public float getMass() {
        if (mass < Float.MIN_VALUE)
            mass = 0;
        return mass + size();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
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
     *
     * @param x input mappig value
     * @return
     */
    public static interface BagCurve {

        public double y(double x);
    }


    public static class CubicBagCurve implements BagCurve {

        @Override
        public final double y(final double x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-       
            return 1.0 - (x * x * x);
        }

    }

    /**
     * Approximates priority -> probability fairness with an exponential curve
     */
    public static class FairPriorityProbabilityCurve implements BagCurve {

        @Override
        public final double y(final double x) {
            return 1 - Math.exp(-5 * x);
        }

    }


    public static class QuadraticBagCurve implements BagCurve {

        @Override
        public final double y(final double x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-       
            return 1 - (x * x);
        }

    }


}
