/*
 * Bag.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.util.bag;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import nars.core.Parameters;
import nars.logic.entity.Item;
import nars.util.bag.DDNodePool.DD;
import nars.util.data.CuckooMap;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Original Bag implementation which distributes items into
 * discrete levels (queues) according to priority
 */
public class LevelBag<E extends Item<K>, K> extends Bag<K, E> {


    /**
     * priority levels
     */
    public final int levels;
    /**
     * firing threshold
     */
    public final int fireCompleteLevelThreshold;

    /**
     * shared DISTRIBUTOR that produce the probability distribution
     */
    final short[] DISTRIBUTOR;



    /**
     * defined in different bags
     */
    final int capacity;
    /**
     * current sum of occupied level
     */
    private float mass;
    /**
     * index to get next level, kept in individual objects
     */
    int levelIndex;
    /**
     * current take out level
     */
    int currentLevel;
    /**
     * maximum number of items to be taken out at current level
     */

    int currentCounter;

    protected final boolean[] levelEmpty;

    private DDNodePool nodePool;

    /**
     * mapping from key to item
     */
    public final Map<K, DD<E>> index;

    /**
     * array of lists of items, for items on different level
     */
    public final Level [] level;

    public static enum NextNonEmptyLevelMode {
        Default, Fast
    }

    NextNonEmptyLevelMode nextNonEmptyMode = Parameters.DEFAULT_LEVEL_BAG_MODE;


    public LevelBag(int levels, int capacity) {
        this(levels, capacity, (int) (Parameters.BAG_THRESHOLD * levels));
    }

    /**
     * thresholdLevel = 0 disables "fire level completely" threshold effect
     */
    public LevelBag(int levels, int capacity, int thresholdLevel) {
        this.levels = levels;

        this.fireCompleteLevelThreshold = thresholdLevel;
        //THRESHOLD = levels + 1; //fair/flat takeOut policy

        this.capacity = capacity;

        //nameTable = Parameters.THREADS == 1 ? Parameters.newHashMap(capacity+1+1) : new ConcurrentHashMap<>(capacity+1+1);
        index = Parameters.THREADS == 1 ?
                new CuckooMap(capacity * 2) :
                new ConcurrentHashMap<>(capacity * 2);

        level = (Level[]) Array.newInstance(Level.class, this.levels);
        nodePool = new DDNodePool(capacity/2);

        levelEmpty = new boolean[this.levels];
        Arrays.fill(levelEmpty, true);

        DISTRIBUTOR = Distributor.get(this.levels).order;

        clear();
    }


    // assumes no calls to DDList.add() during iteration
    public static class DoublyLinkedListIterator<E> implements Iterator<E> {
        private DD<E> current;  // the node that is returned by next()
        private DD<E> lastAccessed;      // the last node to be returned by prev() or next()
        // reset to null upon intervening remove() or add()
        private int index;
        private int size;

        public void init(DDList<E> d) {
            current = d.pre.next;
            lastAccessed = null;
            index = 0;
            size = d.size;
        }
        public boolean hasNext() {
            return index < size;
        }

        public boolean hasPrevious() {
            return index > 0;
        }

        public int previousIndex() {
            return index - 1;
        }

        public int nextIndex() {
            return index;
        }

        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            lastAccessed = current;
            E item = current.item;
            current = current.next;
            index++;
            return item;
        }

        public E previous() {
            if (!hasPrevious()) throw new NoSuchElementException();
            current = current.prev;
            index--;
            lastAccessed = current;
            return current.item;
        }

//            // replace the item of the element that was last accessed by next() or previous()
//            // condition: no calls to remove() or add() after last call to next() or previous()
//            public void set(E item) {
//                if (lastAccessed == null) throw new IllegalStateException();
//                lastAccessed.item = item;
//                changed();
//            }
//
//
//            // remove the element that was last accessed by next() or previous()
//            // condition: no calls to remove() or add() after last call to next() or previous()
//            public void remove() {
//                changed();
//                throw new RuntimeException("not fully implemented");
//                /*
//                if (lastAccessed == null) throw new IllegalStateException();
//                DDNode x = lastAccessed.prev;
//                DDNode y = lastAccessed.next;
//                x.next = y;
//                y.prev = x;
//                N--;
//                if (current == lastAccessed)
//                    current = y;
//                else
//                    index--;
//                lastAccessed = null;
//                */
//            }
//
//
//            // add element to list
//            public void add(E item) {
//                changed();
//                throw new RuntimeException("not fully implemented");
//                /*
//                DDNode x = current.prev;
//                DDNode y = pool.get();
//                DDNode z = current;
//                y.item = item;
//                x.next = y;
//                y.next = z;
//                z.prev = y;
//                y.prev = x;
//                N++;
//                index++;
//                lastAccessed = null;
//                */
//            }
//

    }

    /** high performance linkedhashset/deque for use as a levelbag level */
    public class Level extends DDList<E> {


        public Level(int level) {
            super(level, nodePool);
        }

        @Override
        public void changed() {

            levelEmpty[this.getID()] = isEmpty();

        }

        public E removeFirst() {
            DD<E> first = getFirstNode();
            if (first == null) return null;
            return remove(first);
        }

        public void print() {
            System.out.println("head=" + super.getFirst() + ", tail=" + super.getLast() + ", ");
            System.out.println("  " + Lists.newArrayList(iterator()));
        }

        /*public E removeFirst() {
            E e = getFirst();
            if (e == null) return null;
            return remove(e);
        }*/

        public E peekFirst() {
            return getFirst();
        }

        public Iterator<E> descendingIterator() {
            //order wont matter within the level
            return iterator();
            //return items.descendingIterator();
        }


    }

    @Override
    public final void clear() {

        index.clear();
        for (int i = 0; i < levels; i++) {
            if (level[i] != null) {
                level[i].clear();
            }
        }

        currentLevel = levels - 1;
        levelIndex = capacity % levels; // so that different bags start at different point
        mass = 0;
        currentCounter = 0;
    }

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    @Override
    public int size() {

        int in = index.size();

        if (Parameters.DEBUG_BAG && (Parameters.DEBUG) && (Parameters.THREADS == 1)) {

            int is = sizeItems();
            if (Math.abs(is - in) > 1) {
                throw new RuntimeException(this.getClass() + " inconsistent index: items=" + is + " names=" + in + ", capacity=" + getCapacity());

            }
        }

        return in;
    }

    /**
     * this should always equal size(), but it's here for testing purposes
     */
    protected int sizeItems() {
        int t = 0;
        for (Level l : level) {
            if (l != null)
                t += l.size();
        }
        return t;
    }


    @Override
    public Set<K> keySet() {
        return index.keySet();
    }

    /**
     * Get the average priority of Items
     *
     * @return The average priority of Items in the bag
     */
    @Override
    public float getAveragePriority() {
        if (size() == 0) {
            return 0.01f;
        }
        float f = mass / (size());
        if (f > 1) {
            return 1.0f;
        }
        return f;
    }

    @Override
    public E TAKE(final K name) {
        DD<E> t = index.get(name);
        if (t == null) return null;
        return OUT(t);
    }

    @Override
    public E PUT(BagSelector<K, E> selector) {
        return super.PUT(selector);
        //return super.putInFast(selector);
    }

    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    @Override
    public E GET(final K key) {
        DD<E> b = index.get(key);
        if (b==null) return null;
        return b.item;
    }


//    /**
//     * Check whether a level is empty
//     *
//     * @param n The level index
//     * @return Whether that level is empty
//     */
//    final public boolean levelEmpty[final int l) {
//        final Deque<E> level = this.level[l];
//        return (level == null) || (level.isEmpty());
//    }

    public LevelBag setNextNonEmptyMode(NextNonEmptyLevelMode nextNonEmptyMode) {
        this.nextNonEmptyMode = nextNonEmptyMode;
        return this;
    }


    protected void nextNonEmptyLevel() {
        switch (nextNonEmptyMode) {
            case Default:
                nextNonEmptyLevelDefault();
                break;
            case Fast:
                nextNonEmptyLevelFast();
                break;
        }
    }


    /**
     * look for a non-empty level
     */
    protected void nextNonEmptyLevelDefault() {

        //cache class fields as local variables for speed in the iteration
        final int numLevels = DISTRIBUTOR.length;
        int li = levelIndex;

        while (levelEmpty[ DISTRIBUTOR[li++ % numLevels]]);

        currentLevel = DISTRIBUTOR[(li-1) % numLevels];
        levelIndex = li;

        if (currentLevel < fireCompleteLevelThreshold) { // for dormant levels, take one item
            currentCounter = 1;
        } else {                  // for active levels, take all current items
            currentCounter = getNonEmptyLevelSize(currentLevel);
        }
    }

    /**
     * Variation of LevelBag which follows a different distributor policy but
     * runs much faster.  The policy should be approximately equally fair as LevelBag
     */
    protected boolean nextNonEmptyLevelFast() {

        if (Parameters.DEBUG) {
            boolean actuallyAnyNonEmpty = false;
            for (int i = 0; i < levelEmpty.length; i++) {
                if (!levelEmpty[i]) {
                    actuallyAnyNonEmpty = true;
                    break;
                }
            }
            if (!actuallyAnyNonEmpty) {
                //throw new RuntimeException("inconsistent empty state");
                new RuntimeException("inconsistent empty state").printStackTrace();
                return false;
            }
        }

        int cl = DISTRIBUTOR[(levelIndex++) % DISTRIBUTOR.length];
        if (cl % 2 == 0) {
            //up
            while (levelEmpty[cl]) {
                cl++;
                cl %= levels;
            }
        } else {
            //down
            while (levelEmpty[cl]) {
                cl--;
                if (cl < 0) cl = levelEmpty.length - 1;
            }
        }


        currentLevel = cl;

        if (currentLevel < fireCompleteLevelThreshold) { // for dormant levels, take one item
            currentCounter = 1;
        } else {                  // for active levels, take all current items
            currentCounter = getNonEmptyLevelSize(currentLevel);
        }

        return true;
    }

    @Override
    public E UPDATE(BagSelector<K, E> selector) {

        K key = selector.name();
        DD<E> bx = index.get(key);
        if ((bx == null) || (bx.item == null)) {
            //allow selector to provide a new instance
            E n = selector.newItem();
            if (n!=null) {
                E overflow = PUT(n);
                if (overflow!=null)
                    selector.overflow(overflow);
                return n; //return the new instance
            }
            //no instance provided, nothing to do
            return null;
        }

        E b = bx.item;

        /*if (b == null) {
            printAll();
            throw new RuntimeException("index provided a node with null value");
        }*/

        //allow selector to modify it, then if it returns non-null, reinsert
        //TODO maybe divide this into a 2 stage transaction that can be aborted before the unlevel begins
        E c = selector.updateItem(b);
        if (c!=null) {
            relevel(bx, c);
        }

        return c;
    }

    @Override
    public E PEEKNEXT() {
        if (size() == 0) return null; // empty bag

        E e = TAKENEXT();
        PUT(e);
        return e;
    }

    public E peekNextWithoutAffectingBagOrder() {
        if (size() == 0) return null; // empty bag

        if (levelEmpty[currentLevel] || (currentCounter == 0)) { // done with the current level
            nextNonEmptyLevel();
        }

        return level[currentLevel].peekFirst();
    }

    private synchronized E TAKE(int outLevel) {

        Level l = level[outLevel];
        if (l == null)
            throw new RuntimeException("Attempted TAKE from empty (null) level " + outLevel);
        if (l.isEmpty())
            throw new RuntimeException("Attempted TAKE from empty level " + outLevel);


        DD<E> fn = l.getFirstNode();
        if (fn == null)
            throw new RuntimeException("Attempted TAKE from empty level " + outLevel);

        return OUT(fn);
    }

    @Override
    public synchronized E TAKENEXT() {

        if (size() == 0)
            return null; // empty bag


        if (levelEmpty[currentLevel] || (currentCounter == 0)) { // done with the current level
            nextNonEmptyLevel();
        }

        if (levelEmpty[currentLevel] || (level[currentLevel] == null) || (level[currentLevel].isEmpty())) {
            if (Parameters.THREADS == 1) {
                throw new RuntimeException("Empty setLevel selected for takeNext");
            } else {
                return null;
            }
        }

        // take out the first item in the level
        final E e = TAKE(currentLevel);

        currentCounter--;

        return e;
    }

    public int getNonEmptyLevelSize(final int level) {
        return this.level[level].size();
    }

    public int getLevelSize(final int level) {
        return (levelEmpty[level]) ? 0 : this.level[level].size();
    }





    /**
     * Decide the put-in level according to priority
     *
     * @param item The Item to put in
     * @return The put-in level
     */
    public int getLevel(final E item) {
        final float fl = item.getPriority() * levels;
        final int level = (int) Math.ceil(fl) - 1;
        if (level < 0) return 0;
        if (level >= levels) return levels - 1;
        return level;
    }


    /** removes from existing level and adds to new one */
    protected synchronized DD<E> relevel(DD<E> x, E newValue) {
        int prevLevel = x.owner();
        int nextLevel = getLevel(newValue);

        E prevValue = x.item;

        boolean keyChange = !newValue.name().equals(prevValue.name());

        if (keyChange) {
            //name changed, must be rehashed
            OUT(x);
            IN(newValue);
        }
        else {
            if (prevLevel == nextLevel)
                return x;

            level[prevLevel].detach(x);
            x.owner = nextLevel;
            x.item = newValue;
            ensureLevelExists(nextLevel).add(x);
            return x;
        }




        return x;
    }

    /** removal of the bagged item from its level and the index */
    public synchronized  E OUT(DD<E> node) {
        if (node == null)
            throw new RuntimeException("OUT must not be null");
        int lev = node.owner();
        if (lev == -1)
            throw new RuntimeException(node + " has invalid level");
        removeMass(node.item);
        E i = node.item;
        level[lev].remove(node);
        index.remove(i.name());
        return i;
    }

    /** addition of the item to its level and the index */
    public synchronized DD<E> IN(E newItem, int inLevel) {
        if (newItem == null)
            throw new RuntimeException("IN must not be null");
        addMass(newItem);
        DD<E> dd = ensureLevelExists(inLevel).add(newItem);
        this.index.put(newItem.name(), dd);
        return dd;
    }

    public synchronized DD<E> IN(E newItem) {
        return IN(newItem, getLevel(newItem));
    }


    @Override
    public E PUT(final E newItem) {
        if (newItem==null)
            throw new RuntimeException("PUT item muts be non-null");

        E overflow = null;

        //1. ensure capacity
        int inLevel = getLevel(newItem);
        if (size() >= capacity) {      // the bag will be full after the next
            int outLevel = 0;
            while (levelEmpty[outLevel]) {
                outLevel++;
            }
            if (outLevel > inLevel) {           // ignore the item and exit
                return newItem;
            } else {                            // remove an old item in the lowest non-empty level
                overflow = TAKE(outLevel);
            }
        }

        //2.
        ensureLevelExists(inLevel);

        //3. insert
        /*Bagged dd = */IN(newItem, inLevel);

        //4.
        return overflow;
    }



    protected final Level ensureLevelExists(final int l) {
        if (this.level[l] == null) {
            this.level[l] = new Level(l);
        }
        return level[l];
    }


//    /**
//     * Take out the first or last E in a level from the itemTable
//     *
//     * @param level The current level
//     * @return The first Item
//     */
//    private E takeOutFirst(final int level) {
//        final E selected = this.level[level].removeFirst();
//        if (selected != null) {
//            E removed = index.remove(selected.name());
//            removeMass(selected);
//        } else {
//            if (Parameters.THREADS == 1) {
//                throw new RuntimeException("Attempt to remove item from empty setLevel: " + level);
//            }
//        }
//        return selected;
//    }

    protected void removeMass(E item) {
        mass -= item.getPriority();
    }

    protected void addMass(E item) {
        mass += item.getPriority();
    }


//    /**
//     * Refresh display
//     */
//    @Deprecated protected void refresh() {
//        if (bagObserver!=null)       
//            if (bagObserver.isActive()) {
//                bagObserver.refresh(toString());
//            }
//    }


    /**
     * Collect Bag content into a String for display
     */
//    @Override
//    public String toString() {
//        final StringBuilder buf = new StringBuilder(" ");
//        for (int i = levels - 1; i >= 0; i--) {
//            if (itemTable[i] != null && !itemTable[i].isEmpty()) {
//                buf.append("\n --- Level ").append((i + 1)).append(":\n");
//                for (final E e : itemTable[i]) {
//                    buf.append(e.toStringExternal()).append('\n');
//                }
//            }
//        }
//        return buf.toString();
//    }

    /**
     * TODO refactor : paste from preceding method
     */
    public String toStringLong(int minLevel) {
        StringBuilder buf = new StringBuilder(32)
                .append(" BAG ").append(getClass().getSimpleName())
                .append(" ").append(showSizes());

        for (int i = levels; i >= minLevel; i--) {
            if (!levelEmpty[i - 1]) {
                buf.append("\n --- LEVEL ").append(i).append(":\n ");
                for (final E e : level[i - 1]) {
                    buf.append(e.toStringLong()).append('\n');
                }

            }
        }
        buf.append(">>>> end of Bag").append(getClass().getSimpleName());
        return buf.toString();
    }

    /**
     * show item Table Sizes
     */
    public String showSizes() {
        StringBuilder buf = new StringBuilder(" ");
        int l = 0;
        for (Level items : level) {
            int s = items.size();
            if (s > 0) {
                l++;
                buf.append(s).append(' ');
            }
        }
        return "Levels: " + Integer.toString(l) + ", sizes: " + buf;
    }

    @Override
    public float getMass() {
        return mass;
    }

    public float getAverageItemsPerLevel() {
        return ((float) capacity) / levels;
    }

    public float getMaxItemsPerLevel() {
        int max = getLevelSize(0);
        for (int i = 1; i < levels; i++) {
            int s = getLevelSize(i);
            if (s > max) {
                max = s;
            }
        }
        return max;
    }

    public float getMinItemsPerLevel() {
        int min = getLevelSize(0);
        for (int i = 1; i < levels; i++) {
            int s = getLevelSize(i);
            if (s < min) {
                min = s;
            }
        }
        return min;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    Iterable<E> getLevel(final int i) {
        if (level[i] == null) {
            return Collections.EMPTY_LIST;
        }
        return level[i];
    }

    @Override
    public Collection<E> values() {
        throw new RuntimeException("Coming soon");
        //return index.values();
    }


    final Function<Level, Iterator<E>> levelIteratorFunc = new Function<Level,Iterator<E>>() {

        final Iterator<E> empty = Collections.EMPTY_LIST.iterator();

        @Override
        public Iterator<E> apply(Level o) {
            if (o == null) return empty;
            return o.iterator();
        }
    };

    /** recyclable reversable array iterator
     *
     * modified from: http://faculty.washington.edu/moishe/javademos/ch03%20Code/jss2/ArrayIterator.java
     * */
    public static class ReversibleRecyclableArrayIterator<T> implements Iterator<T> {
        private int count;    // the number of elements in the collection
        private int current;  // the current position in the iteration
        private T[] a;
        private boolean reverse;

        public ReversibleRecyclableArrayIterator(T[] array, boolean reverse) {
            init(array, array.length, reverse);
        }

        public ReversibleRecyclableArrayIterator(T[] array, int size, boolean reverse) {
            init(array, size, reverse);
        }

        public void init(T[] array, int size, boolean reverse) {
            a = array;
            count = size;
            current = 0;
            this.reverse = reverse;
        }

        public boolean hasNext() {
            return (current < count);
        }

        public T next() {
            if (!hasNext()) throw new NoSuchElementException();

            int i = (reverse) ? ((count-1) - current) : current;
            current++;

            return a[i];
        }

        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }


    @Override
    @Deprecated public Iterator<E> iterator() {
        return Iterators.concat(
                Iterators.transform(
                        new ReversibleRecyclableArrayIterator(level, true), levelIteratorFunc)  );
        //return new ItemIterator();
        //return new DDIterators();
    }

    @Override
    public void forEach(Consumer<? super E> c) {
        int count = size();

        if (count == 0) return;

        for (int l = level.length-1; l >= 0; l--) {
            if (levelEmpty[l]) continue;
            DD<E> node = level[l].getFirstNode();
            while (node!=null) {
                if (node.item!=null)
                    c.accept( node.item );
                node = node.next;
            }
        }
    }


//    // assumes no calls to DDList.add() during iteration
//    private class DDIterators implements Iterator<E> {
//        private DD<E> current;  // the node that is returned by next()
//        private DD<E> lastAccessed = null;      // the last node to be returned by prev() or next()
//        // reset to null upon intervening remove() or add()
//        private int index;
//        private Level currentLevel;
//        int currentLevelNum = 0;
//
//        DDIterators() {
//            nextLevel();
//        }
//
//        public void nextLevel() {
//            currentLevel = level[currentLevelNum];
//            current = currentLevel.pre.next;
//            lastAccessed = null;
//            index = 0;
//        }
//
//        public boolean hasNext() {
//            while (!((index < currentLevel.size))) {
//            if  return true;
//            else {
//                currentLevelNum++;
//                nextLevel();
//
//            }
//        }
//
//        public boolean hasPrevious() {
//            return index > 0;
//        }
//
//        public int previousIndex() {
//            return index - 1;
//        }
//
//        public int nextIndex() {
//            return index;
//        }
//
//        public E next() {
//            if (!hasNext()) throw new NoSuchElementException();
//            lastAccessed = current;
//            E item = current.item;
//            current = current.next;
//            index++;
//            return item;
//        }
//
//        public E previous() {
//            if (!hasPrevious()) throw new NoSuchElementException();
//            current = current.prev;
//            index--;
//            lastAccessed = current;
//            return current.item;
//        }
//
//
//
//
//    }


    public int numEmptyLevels() {
        int empty = 0;
        for (int i = 0; i < level.length; i++) {
            if (levelEmpty[i]) {
                empty++;
            }
        }
        return empty;
    }

    abstract public static class DequePool<X> {
        final Deque<X> data = new ArrayDeque();

        public DequePool(int preallocate) {
            for (int i = 0; i < preallocate; i++)
                put(create());
        }

        public void put(X i) {
            data.offer(i);
        }

        public X get() {
            if (data.isEmpty()) return create();
            return data.poll();
        }

        abstract public X create();


    }

    //TODO move this to a "bag metrics" class
//    private void stat() {
//        int itsize = 0;
//        Set<CharSequence> items = new HashSet();
//        for (Deque<E> x : itemTable) {
//            if (x != null) {
//                itsize += x.size();
//                for (E e : x)
//                    if (e!=null) items.add(e.name());
//            }
//        }
//        int nsize = nameTable.size();
//        if (nsize != itsize) {
//            System.err.println("nameTable=" + nameTable.size() + " , itemTable=" + itsize);
////            Set<CharSequence> named = new HashSet(nameTable.keySet());
////            if (itsize > nsize) {
////                System.err.println("  itemTable extras: " + items.removeAll(named));
////            }
////            else {
////                System.err.println("  nameTable extras: " + named.removeAll(items));
////            }
//                        
//        }
//        
//    }


}



//    final private class ItemIterator implements Iterator<E> {
//
//        int l = level.length - 1;
//        private Iterator<E> levelIterator;
//        private E next;
//        final int size = size();
//        int count = 0;
//
//        @Override
//        public boolean hasNext() {
//            if (next != null) {
//                return true;
//            }
//
//            if (l >= 0 && levelIterator == null) {
//                while (levelEmpty[l]) {
//                    if (--l == -1)
//                        return false; //end of the levels
//                }
//                levelIterator = level[l].descendingIterator();
//            }
//
//            if (levelIterator == null) {
//                return false;
//            }
//
//            next = levelIterator.next();
//            count++;
//
//            if (levelIterator.hasNext()) {
//                return true;
//            } else {
//                levelIterator = null;
//                l--;
//                return count <= size;
//            }
//        }
//
//        @Override
//        public E next() throws NoSuchElementException {
//            if (next == null) throw new NoSuchElementException();
//            E e = next;
//            next = null;
//            return e;
//        }
//
//    }


