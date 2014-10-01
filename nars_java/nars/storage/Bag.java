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
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import nars.core.Param.AtomicDurations;
import nars.core.Parameters;
import nars.entity.Item;


public class Bag<E extends Item> extends AbstractBag<E>  {

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
    public final Map<CharSequence, E> nameTable;

    /**
     * array of lists of items, for items on different level
     */
    public final Deque<E>[] itemTable;
    
    /**
     * defined in different bags
     */
    final int capacity;
    /**
     * current sum of occupied level
     */
    private int mass;
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

    
    
    /**
     * The display level; initialized at lowest
     */
    //private int showLevel;
    
    protected Bag(int levels, int capacity) {
        this(levels, capacity, null);
    }
    
    public Bag(int levels, int capacity, AtomicDurations forgettingRate) {        
        this.levels = levels;
        this.forgettingRate = forgettingRate;
        
        THRESHOLD = (int)(Parameters.BAG_THRESHOLD * levels);
        //THRESHOLD = levels + 1; //fair/flat takeOut policy
        
        this.capacity = capacity;
        
        nameTable = new HashMap<>(capacity);
        
        itemTable = new Deque[this.levels];
        
        DISTRIBUTOR = Distributor.get(this.levels).order;
        
        clear();
        //showing = false;        
    }
    

    @Override
    public final void clear() {
        for (int i = 0; i < levels; i++) {            
            if (itemTable[i]!=null)
                itemTable[i].clear();
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
        return nameTable.size();
    }

    @Override
    public Set<CharSequence> keySet() {
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
        float f = (float) mass / (size() * levels);
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
    public E get(final CharSequence key) {
        return nameTable.get(key);
    }


    @Override 
    public boolean putIn(final E newItem, final boolean nameTableInsert) {
                        
        if (nameTableInsert) {
            final CharSequence newKey = newItem.name();
            final E oldItem = nameTable.put(newKey, newItem);            
            if (oldItem != null) {                  // merge duplications
                outOfBase(oldItem);
                newItem.merge(oldItem);
            }
        }        
        
        final E overflowItem = intoBase(newItem);  // put the (new or merged) item into itemTable
        if (overflowItem != null) {             // remove overflow
            final CharSequence overflowKey = overflowItem.name();
            nameTable.remove(overflowKey);
            return (overflowItem != newItem);
        } else {
            return true;
        }
        
    }
    


    /**
     * Check whether a level is empty
     *
     * @param n The level index
     * @return Whether that level is empty
     */
    final public boolean levelEmpty(final int l) {
        final Deque<E> level = itemTable[l];
        return (level == null) || (level.isEmpty());
    }

    
    final public int nextNonEmptyLevel(int levelIndex) {
        //group this code into one function so it hopefully gets inlined */
        final int distributorLength = DISTRIBUTOR.length;
        Deque<E> currentTable;
        int cl = currentLevel;
        
        do {
            cl = DISTRIBUTOR[((levelIndex++)%distributorLength)];
            currentTable = itemTable[cl];
        } while ((currentTable==null) || (currentTable.isEmpty())); //levelEmpty(currentLevel));
        
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
            } while (levelEmpty(currentLevel));
            levelIndex = (levelIndex + levelsTraversed) % DISTRIBUTOR.length;
        }   
        else {
            levelIndex = nextNonEmptyLevel(levelIndex);
        }

        if (currentLevel < THRESHOLD) { // for dormant levels, take one item
            currentCounter = 1;
        } else {                  // for active levels, take all current items
            currentCounter = getLevelSize(currentLevel);
        }
    }
    

    @Override
    public E takeOut(final boolean removeFromNameTable) {
        if (size() == 0) return null; // empty bag                
                
        if (levelEmpty(currentLevel) || (currentCounter == 0)) { // done with the current level
            nextNonEmptyLevel();
        }
        
        final E selected = takeOutFirst(currentLevel); // take out the first item in the level
               
        currentCounter--;
        
        if (removeFromNameTable) {
            nameTable.remove(selected.name());
            //refresh();
        }
        
        return selected;
    }
    

    public int getLevelSize(final int level) {        
        return (levelEmpty(level)) ? 0 : itemTable[level].size();
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
     * Decide the put-in level according to priority
     *
     * @param item The Item to put in
     * @return The put-in level
     */
    private int getLevel(final E item) {
        final float fl = item.getPriority() * levels;
        final int level = (int) Math.ceil(fl) - 1;
        return (level < 0) ? 0 : level;     // cannot be -1
    }

    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param newItem The Item to put in
     * @return The overflow Item
     */
    private E intoBase(final E newItem) {
        E oldItem = null;
        int inLevel = getLevel(newItem);
        if (size() > capacity) {      // the bag is full
            int outLevel = 0;
            //while (itemTable[outLevel].isEmpty()) {
            while (levelEmpty(outLevel)) {
                outLevel++;
            }
            if (outLevel > inLevel) {           // ignore the item and exit
                return newItem;
            } else {                            // remove an old item in the lowest non-empty level
                oldItem = takeOutFirst(outLevel);
            }        
        }
        ensureLevelExists(inLevel);
        itemTable[inLevel].add(newItem);        // FIFO
        mass += (inLevel + 1);                  // increase total mass
        //refresh();                              // refresh the window
        return oldItem;		// TODO return null is a bad smell
    }

    protected final void ensureLevelExists(final int level) {
        if (itemTable[level]==null)
            itemTable[level] = newLevel();
    }
    
    protected Deque<E> newLevel() {
        return new ArrayDeque<>(1+capacity/levels);
        //return new LinkedList<E>();  //not good
        //return new FastTable<E>(); //slower than arraydeque under current loads    
    }
    
    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    private E takeOutFirst(final int level) {
        final E selected = itemTable[level].removeFirst();        
        mass -= (level + 1);
        //refresh();
        return selected;
    }

    /**
     * Remove an item from itemTable, then adjust mass
     *
     * @param oldItem The Item to be removed
     */
    protected void outOfBase(final E oldItem) {
        final int level = getLevel(oldItem);
        
        //boolean found = false;
        if (itemTable[level] != null)
            if (itemTable[level].remove(oldItem)) {
                //found = true;
                mass -= (level + 1);
            }
        
        /*
        //If it wasn't found, it probably was removed already.  So this check is probably not necessary
        
        if (!found) {
            //search other levels for this item because it's not where we thought it was according to getLevel()
            if (!outOfBaseComplete(oldItem)) {
                throw new RuntimeException("Can not remove missing element " + oldItem + " from " + this.getClass().getSimpleName());
            }
        }*/
        
        //refresh();
    }

    /** try to avoid calling this, it is expensive.  
     * a bug in an outOfBase() caller might be causing the need for this. */
    protected boolean outOfBaseComplete(final E oldItem) {
        for (int i = 0; i < levels; i++) {
            if (itemTable[i]!=null)
                if (itemTable[i].remove(oldItem))
                    return true;
        }       
        return false;
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
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(" ");
        for (int i = levels-1; i >= 0; i--) {
            if (itemTable[i]!=null && !itemTable[i].isEmpty()) {
                buf.append("\n --- Level ").append((i+1)).append(":\n");
                for (final E e : itemTable[i]) {
                    buf.append(e.toStringExternal()).append('\n');
                }
            }
        }
        return buf.toString();
    }

    /**
     * TODO refactor : paste from preceding method
     */
    public String toStringLong(int minLevel) {
        StringBuilder buf = new StringBuilder(32)
            .append(" BAG ").append(getClass().getSimpleName())
            .append(" ").append(showSizes());
        
        for (int i = levels; i >= minLevel; i--) {
            if (!levelEmpty(i-1)) {
                buf = buf.append("\n --- LEVEL ").append(i).append(":\n ");
                for (final E e : itemTable[i-1]) {
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
        for (Collection<E> items : itemTable) {
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
        return capacity/levels;
    }
    public float getMaxItemsPerLevel() {
        int max = getLevelSize(0);
        for (int i = 1; i < levels; i++) {
            int s = getLevelSize(i);
            if (s > max) max = s;
        }
        return max;
    }
    public float getMinItemsPerLevel() {
        int min = getLevelSize(0);
        for (int i = 1; i < levels; i++) {
            int s = getLevelSize(i);
            if (s < min) min = s;
        }
        return min;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    public Collection<E> getLevel(final int i) {
        if (itemTable[i] == null) return Collections.EMPTY_LIST;
        return itemTable[i];
    }

    @Override
    public Collection<E> values() {
        return nameTable.values();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            int l = itemTable.length-1;            
            private Iterator<E> levelIterator;
            private E next;
            final int size = size();
            int count = 0;
            
            @Override
            public boolean hasNext() {
                if (next!=null)
                    return true;
                
                if (l >=0 && levelIterator == null) {
                    while (levelEmpty(l)) {
                        l--;
                        if (l == -1)
                            return false; //end of the levels
                    }
                    levelIterator = itemTable[l].descendingIterator();
                }
                
                if (levelIterator == null)
                    return false;
                
                next = levelIterator.next();
                count++;                                
                
                if (levelIterator.hasNext()) {
                    return true;
                }
                else {
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
        for (int i = 0; i < itemTable.length; i++) {
            if (levelEmpty(i))
                empty++;
        }
        return empty;
    }

    @Override public E removeKey(final CharSequence key) {
        return nameTable.remove(key);
    }
        
    
    
    
}
