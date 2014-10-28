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
package nars.storage;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import nars.core.Parameters;
import nars.entity.Item;

/**
 * Original Bag implementation which distributes items into
 * discrete levels (queues) according to priority
 */
public class LevelBag<E extends Item<K>,K> extends Bag<E,K> {

    /**
     * priority levels
     */
    public final int levels;
    /**
     * firing threshold
     */
    public final int THRESHOLD;

    /**
     * shared DISTRIBUTOR that produce the probability distribution
     */
    private final short[] DISTRIBUTOR;

    /**
     * mapping from key to item
     */
    //public final Set<E> nameTable;
    public final Map<K, E> nameTable;

    /**
     * array of lists of items, for items on different level
     */
    public final Deque<E>[] level;

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
    private int levelIndex;
    /**
     * current take out level
     */
    private int currentLevel;
    /**
     * maximum number of items to be taken out at current level
     */
    private int currentCounter;
    private final boolean[] levelEmpty;



    public LevelBag(int levels, int capacity) {
        this.levels = levels;

        THRESHOLD = (int) (Parameters.BAG_THRESHOLD * levels);
        //THRESHOLD = levels + 1; //fair/flat takeOut policy

        this.capacity = capacity;

        nameTable = new HashMap<>(capacity);

        level = new Deque[this.levels];
        
        levelEmpty = new boolean[this.levels];
        Arrays.fill(levelEmpty, true);

        DISTRIBUTOR = Distributor.get(this.levels).order;

        clear();
    }

    public class Level extends ArrayDeque<E> {
        private final int thisLevel;

        public Level(int level, int numElements) {
            super(numElements);
            this.thisLevel = level;
        }
        
        void levelIsEmpty(final boolean e) {
            levelEmpty[thisLevel] = e;
        }

        @Override
        public void clear() {
            super.clear();
            levelIsEmpty(true);
        }
        
        
        @Override public boolean add(final E e) {
            if (super.add(e)) {
                levelIsEmpty(false);
                return true;
            }
            return false;
        }

        @Override
        public boolean remove(Object o) {
            if (super.remove(o)) {
                levelIsEmpty(isEmpty());
                return true;
            }
            return false;
        }

        @Override
        public E removeFirst() {
            E e = super.removeFirst();
            if (e!=null) {
                levelIsEmpty(isEmpty());
            }
            return e;
        }
        
        
        
    }
    
    private Deque<E> newLevel(int l) {
        return new Level(l, 1 + capacity / levels);
        
        //return new LinkedList<E>();  //not good
        //return new FastTable<E>(); //slower than arraydeque under current loads    
    }
    
    @Override
    public final void clear() {
        for (int i = 0; i < levels; i++) {
            if (level[i] != null) {
                level[i].clear();
            }
        }
        nameTable.clear();
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
        int is = sizeItems();
        int in = nameTable.size();
        if (is!=in) {
            System.err.println(this.getClass() + " inconsistent index: items=" + is + " names=" + in + ", capacity=" + getCapacity());                
            new Exception().printStackTrace();            
            //printAll();
            System.exit(1);
            
        }
        
        return nameTable.size();
    }
        
    /** this should always equal size(), but it's here for testing purposes */
    protected int sizeItems() {
        int t = 0;
        for (Deque<E> l : level) {
            if (l!=null)
                t += l.size();
        }
        return t;
    }

    
    @Override
    public Set<K> keySet() {
        return nameTable.keySet();
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
        float f = (float) mass / (size());
        if (f > 1) {
            return 1.0f;
        }
        return f;
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

    final public int nextNonEmptyLevel(int levelIndex) {
        //group this code into one function so it hopefully gets inlined */
        final int distributorLength = DISTRIBUTOR.length;        
        int cl = currentLevel;

        do {
            cl = DISTRIBUTOR[((levelIndex++) % distributorLength)];            
        } while (levelEmpty[cl]); //levelEmpty[currentLevel));

        currentLevel = cl; //write to object outside of the loop

        return levelIndex;
    }

    protected void nextNonEmptyLevel() {
        // look for a non-empty level
        if (size() == 1) {
            //optimized case: only one item - just find the next non-empty level
            int levelsTraversed = 0;
            do {
                currentLevel = (currentLevel + 1) % levels;
                levelsTraversed++;
            } while (levelEmpty[currentLevel]);
            levelIndex = (levelIndex + levelsTraversed) % DISTRIBUTOR.length;
        } else {
            levelIndex = nextNonEmptyLevel(levelIndex);
        }

        if (currentLevel < THRESHOLD) { // for dormant levels, take one item
            currentCounter = 1;
        } else {                  // for active levels, take all current items
            currentCounter = getLevelSize(currentLevel);
        }
    }

    @Override
    public E peekNext() {
        if (size() == 0) return null; // empty bag                
        
        
        if (levelEmpty[currentLevel] || (currentCounter == 0)) { // done with the current level
            nextNonEmptyLevel();
        }
       
        return level[currentLevel].peekFirst();
    }
    
    @Override
    public E takeNext() {
        
        if (size() == 0) {
            return null; // empty bag                
        }
        
        if (levelEmpty[currentLevel] || (currentCounter == 0)) { // done with the current level
            nextNonEmptyLevel();
        }
        
        if (levelEmpty[currentLevel]) {
            System.err.println("Empty level selected for takeNext");
            showSizes();
            //printAll();
            System.exit(1);
        }

        int nspre = nameTable.size();
        
        final E selected = takeOutFirst(currentLevel); // take out the first item in the level
        
        if (nameTable.size() != nspre - 1) {
            System.err.println("could not remove nametable entry for: " + selected);
            size();            
        }
        
        currentCounter--;
        

        return selected;
    }

    public int getLevelSize(final int level) {
        return (levelEmpty[level]) ? 0 : this.level[level].size();
    }



    @Override public E take(final K name) {
        
        E oldItem = nameTable.remove(name);
        if (oldItem == null) {
            return null;
        }
        
        
        final int expectedLevel = getLevel(oldItem);

        //TODO scan up/down iteratively, it is likely to be near where it was
        
        if (!levelEmpty[expectedLevel]) {
            if (level[expectedLevel].remove(oldItem)) {                
                removeMass(oldItem);
                return oldItem;
            }            
        }
        
        for (int l = 0; l < levels; l++) {
            if ((!levelEmpty[l]) && (l!=expectedLevel)) {
                if (level[l].remove(oldItem)) {
                    removeMass(oldItem);
                    return oldItem;
                }
            }
        }

        
        //If it wasn't found, it probably was removed already.  So this check is probably not necessary
        
            //search other levels for this item because it's not where we thought it was according to getLevel()
        String m = "Possible LevelBag inconsistency: Can not remove missing element: size inconsistency" + oldItem + " from " + this.getClass().getSimpleName();

        System.err.println(m);
        size();
        //refresh();
        return oldItem;
    }



    /**
     * Decide the put-in level according to priority
     *
     * @param item The Item to put in
     * @return The put-in level
     */
    private int getLevel(final E item) {
        final float fl = item.getPriority() * levels;
        final int level = (int) Math.ceil(fl) - 1;
        if (level < 0) return 0;
        if (level >= levels) return levels-1;
        return level;
    }

    
    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param newItem The Item to put in
     * @return null if nothing overflowed, non-null if an overflow Item, which
     * may be the attempted input item (in which case it was not inserted)
     */
    @Override protected E addItem(final E newItem) {
        E oldItem = null;
        int inLevel = getLevel(newItem);
        if (size() >= capacity) {      // the bag will be full after the next 
            int outLevel = 0;
            while (levelEmpty[outLevel]) {
                outLevel++;
            }
            if (outLevel > inLevel) {           // ignore the item and exit
                return newItem;
            } else {                            // remove an old item in the lowest non-empty level
                oldItem = takeOutFirst(outLevel);
            }
        }
        ensureLevelExists(inLevel);
        
        
        level[inLevel].add(newItem);        // FIFO
        nameTable.put(newItem.name(), newItem);        
        addMass(newItem);
        
        return oldItem;
    }

    protected final void ensureLevelExists(final int level) {
        if (this.level[level] == null) {
            this.level[level] = newLevel(level);
        }
    }


    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    private E takeOutFirst(final int level) {
        final E selected = this.level[level].removeFirst();
        nameTable.remove(selected.name());
        removeMass(selected);
        return selected;
    }

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
                buf = buf.append("\n --- LEVEL ").append(i).append(":\n ");
                for (final E e : level[i - 1]) {
                    buf = buf.append(e.toStringLong()).append('\n');
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
        for (Collection<E> items : level) {
            if ((items != null) && (!items.isEmpty())) {
                l++;
                buf.append(items.size()).append(' ');
            }
        }
        return "Levels: " + Integer.toString(l) + ", sizes: " + buf;
    }

    @Override
    public float getMass() {
        return mass;
    }

    public float getAverageItemsPerLevel() {
        return capacity / levels;
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

    public Collection<E> getLevel(final int i) {
        if (level[i] == null) {
            return Collections.EMPTY_LIST;
        }
        return level[i];
    }

    @Override
    public Collection<E> values() {
        return nameTable.values();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            int l = level.length - 1;
            private Iterator<E> levelIterator;
            private E next;
            final int size = size();
            int count = 0;

            @Override
            public boolean hasNext() {
                if (next != null) {
                    return true;
                }

                if (l >= 0 && levelIterator == null) {
                    while (levelEmpty[l]) {
                        l--;
                        if (l == -1) {
                            return false; //end of the levels
                        }
                    }
                    levelIterator = level[l].descendingIterator();
                }

                if (levelIterator == null) {
                    return false;
                }

                next = levelIterator.next();
                count++;

                if (levelIterator.hasNext()) {
                    return true;
                } else {
                    levelIterator = null;
                    l--;
                    return count <= size;
                }
            }

            @Override
            public E next() {
                E e = next;
                next = null;
                return e;
            }

        };
    }

    public int numEmptyLevels() {
        int empty = 0;
        for (int i = 0; i < level.length; i++) {
            if (levelEmpty[i]) {
                empty++;
            }
        }
        return empty;
    }



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
