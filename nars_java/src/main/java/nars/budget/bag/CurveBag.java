package nars.budget.bag;

import nars.Memory;
import nars.Global;
import nars.budget.Budget;
import nars.nal.Item;
import nars.budget.Bag;
import nars.util.data.sorted.ArraySortedIndex;
import nars.util.data.sorted.SortedIndex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Bag which stores items, sorted, in one array.
 * Removal policy can select items by percentile via the array index.
 * A curve function maps a probabilty distribution to an index allowing the bag
 * to choose items with certain probabilities more than others.
 *
 * In theory, the curve can be calculated to emulate any potential removal policy.
 *
 * Insertion into the array is a O(log(n)) insertion sort, plus O(N) to shift items (unless the array is tree-like and can avoid this cost).
 * Removal is O(N) to shift items, and an additional possible O(N) if a specific item to be removed is not found at the index expected for its current priority value.
 *
 * TODO make a CurveSampling interface with at least 2 implementations: Random and LinearScanning. it will use this instead of the 'boolean random' constructor argument
 */
public class CurveBag<E extends Item<K>, K> extends Bag<K, E> {

    final float MASS_EPSILON = 1.0e-5f;

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
        return new ArraySortedIndex(capacity);
        //else
        //    return new FractalSortedItemList<E>();
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
            //CuckooMap<K,E> {  //doesnt seem to work yet in CurveBag

        public CurveMap(int initialCapacity) {
            super(initialCapacity * 1 + 1);
        }

        @Override
        public E put(final K key, final E value) {

            E removed, removed2;

            /*synchronized (nameTable)*/ {

                removed = putKey(key, value);
                if (removed != null) {
                    removeItem(removed);
                }


                removed2 = addItem(value);
                if (removed!=null && removed2!=null) {
                    throw new RuntimeException("Only one item should have been removed on this insert; both removed: " + removed + ", " + removed2);
                }
                if (removed2!=null) {
                    removeKey(removed2.name());
                    return removed2;
                }
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

//        public E removeItem(K name) {
//            /*synchronized (nameTable)*/ {
//                E item = nameTable.get(name);
//                if (item == null) return null;
//                return removeItem(item);
//            }
//        }

        public E removeItem(final E removed) {
            if (items.remove(removed)) {
                mass -= removed.getPriority();
                return removed;
            }
            return null;
        }

        public E addItem(final E i) {
            return items.insert(i);
        }

        @Override
        public E remove(final Object key) {

            E e;

            /*synchronized (nameTable)*/ {

                e = removeKey((K) key);
                if (e != null) {
                    removeItem(e);
                }
                if (Global.DEBUG && Global.DEBUG_BAG)
                    CurveBag.this.size();

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
        /*synchronized (nameTable)*/ {
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

        if (Global.DEBUG) {
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
    public float getPriorityMean() {
        final int s = size();
        if (s == 0) {
            return 0.01f;
        }
        float f = mass / s;
        if (f > 1.0f)
            return 1.0f;
        if (f < 0.01f)
            return 0.01f;
        return f;
    }

    protected void index(E value) {
        /*E oldValue = */
        nameTable.putKey(value.name(), value);
    }

//    protected E unindex(K name) {
//        E removed = nameTable.removeKey(name);
//        return removed;
//    }


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

    @Override
    public E remove(final K key) {
        return nameTable.remove(key);
    }


    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item, or null if this bag is empty
     */
    @Override
    public E pop() {

        if (size() == 0) return null; // empty bag
        return removeItem(nextRemovalIndex());

    }


    @Override
    public E peekNext() {

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
        if (y > 1.0f) y = 1.0f;

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
    public E put(E i) {



            float newPriority = i.getPriority();

            boolean contains = nameTable.containsKey(i.name());
            if ((nameTable.size() >= capacity) && (!contains)) {
                // the bag is full

                // this item is below the bag's already minimum item, no change (return the input as the overflow)
                if (newPriority < getMinPriority()) {
                    return i;
                }

                E oldItem = removeItem(0);

                if (oldItem == null)
                    throw new RuntimeException("required removal but nothing removed");
                else {
                    if (oldItem.name().equals(i.name())) {
                        throw new RuntimeException("this oldItem should have been removed on earlier nameTable.put call: " + oldItem + ", during put(" + i + ")");
                    }
                }


                //insert
                nameTable.put(i.name(), i);

                mass += i.getPriority();

                return oldItem;
            } else if (contains) {
                //TODO check this mass calculation
                E existingToReplace = nameTable.put(i.name(), i);
                mass += i.getPriority();
                return null;
            } else /* if (!contains) */ {
                E shouldNotExist = nameTable.put(i.name(), i);
                if (shouldNotExist != null)
                    throw new RuntimeException("already expected no existing key/item but it was actually there");
                mass += i.getPriority();
                return null;
            }




    }

//
//    protected synchronized E removeItem2(final int index) {
//
//        final E selected;
//
//        selected = items.remove(index);
//        if (selected != null) {
//            E removed = nameTable.removeKey(selected.name());
//
//            if (removed == null)
//                throw new RuntimeException(this + " inconsistent index: items contained " + selected + " but had no key referencing it");
//
//            //should be the same object instance
//            if ((removed != null) && (removed != selected)) {
//                throw new RuntimeException(this + " inconsistent index: items contained " + selected + " and index referenced " + removed + " + ");
//            }
//            mass -= selected.budget.getPriority();
//        } else {
//            throw new RuntimeException(this + " items array returned null item at index " + index);
//        }
//
//        return selected;
//    }


    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    protected E removeItem(final int index) {

        final E selected;

        /*synchronized (nameTable)*/ {

            selected = items.remove(index);
            if (selected == null)
                throw new RuntimeException(this + " inconsistent index: items contained #" + index + " but had no key referencing it");

            //should be the same object instance
            nameTable.removeKey(selected.name());
            mass -= selected.getPriority();
        }

        return selected;
    }


    @Override
    public float mass() {
        if (mass < Float.MIN_VALUE)
            mass = 0;
        return mass + size();
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return super.toString() + '{' + items.getClass().getSimpleName() + '}';
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

        public float y(float x);
    }


    public static class CubicBagCurve implements BagCurve {

        @Override
        public final float y(final float x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-       
            return 1.0f - (x * x * x);
        }

    }

    /**
     * Approximates priority -> probability fairness with an exponential curve
     */
    public static class FairPriorityProbabilityCurve implements BagCurve {

        @Override
        public final float y(final float x) {
            return (float)(1f - Math.exp(-5f * x));
        }

    }


    public static class QuadraticBagCurve implements BagCurve {

        @Override
        public final float y(final float x) {
            //1.0 - ((1.0-x)^2)
            // a function which has domain and range between 0..1.0 but
            //   will result in values above 0.5 more often than not.  see the curve:        
            //http://fooplot.com/#W3sidHlwZSI6MCwiZXEiOiIxLjAtKCgxLjAteCleMikiLCJjb2xvciI6IiMwMDAwMDAifSx7InR5cGUiOjAsImVxIjoiMS4wLSgoMS4wLXgpXjMpIiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTEuMDYyODU2NzAzOTk5OTk5MiIsIjIuMzQ1MDE1Mjk2IiwiLTAuNDM2NTc0NDYzOTk5OTk5OSIsIjEuNjYwNTc3NTM2MDAwMDAwNCJdfV0-       
            return 1f - (x * x);
        }

    }


}
