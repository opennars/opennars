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

import java.util.ArrayList;
import java.util.HashMap;

import nars.entity.Item;
import nars.inference.BudgetFunctions;
import nars.core.Parameters;

/**
 * A Bag is a storage with a constant capacity and maintains an internal
 * priority distribution for retrieval.
 * <p>
 * Each entity in a bag must extend Item, which has a BudgetValue and a key.
 * <p>
 * A name table is used to merge duplicate items that have the same key.
 * <p>
 * The bag space is divided by a threshold, above which is mainly time
 * management, and below, space management. Differences: (1) level selection vs.
 * item selection, (2) decay rate
 *
 * @param <E> The type of the Item in the Bag
 */
public abstract class Bag<E extends Item>  {

    /**
     * priority levels
     */
    public static final int TOTAL_LEVEL = Parameters.BAG_LEVEL;
    /**
     * firing threshold
     */
    public static final int THRESHOLD = Parameters.BAG_THRESHOLD;
    /**
     * relative threshold, only calculate once
     */
    public static final float RELATIVE_THRESHOLD = (float) THRESHOLD / (float) TOTAL_LEVEL;
    /**
     * hashtable load factor
     */
    public static final float LOAD_FACTOR = Parameters.LOAD_FACTOR;       //
    
    //how many items to preallocate in each level
    final int INITIAL_LEVEL_CAPACITY = 0;
    
    /**
     * shared DISTRIBUTOR that produce the probability distribution
     */
    public static final Distributor DISTRIBUTOR = new Distributor(TOTAL_LEVEL); //
    
    
    /**
     * mapping from key to item
     */
    private HashMap<String, E> nameTable;
    /**
     * array of lists of items, for items on different level
     */
    public final ArrayList<E>[] itemTable = new ArrayList[TOTAL_LEVEL];
    
    //this is a cache holding whether itemTable[i] is empty. 
    //it avoids needing to call itemTable.isEmpty() which may improve performance
    //it is maintained each time an item is added or removed from one of the itemTable ArrayLists
    public final boolean[] itemTableEmpty = new boolean[TOTAL_LEVEL];
    
    /**
     * defined in different bags
     */
    private final int capacity;
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
     * reference to memory
     */
    protected final Memory memory;
    
    private BagObserver<E> bagObserver = null;
    
    /**
     * The display level; initialized at lowest
     */
    private int showLevel = Parameters.BAG_THRESHOLD;

    /**
     * constructor, called from subclasses
     *
     * @param memory The reference to memory
     */
    protected Bag(Memory memory) {
        this.memory = memory;
        capacity = capacity();
        init();
        
//        showing = false;
    }

    public void init() {
        for (int i = 0; i < TOTAL_LEVEL; i++) {
            if (INITIAL_LEVEL_CAPACITY > 0)
                itemTable[i] = new ArrayList<E>(INITIAL_LEVEL_CAPACITY);
            else
                itemTable[i] = new ArrayList<E>(); //uses shared empty array when empty
            itemTableEmpty[i] = true;
        }
        nameTable = new HashMap<>((int) (capacity / LOAD_FACTOR), LOAD_FACTOR);
        currentLevel = TOTAL_LEVEL - 1;
        levelIndex = capacity % TOTAL_LEVEL; // so that different bags start at different point
        mass = 0;
        currentCounter = 0;
    }

    /**
     * To get the capacity of the concrete subclass
     *
     * @return Bag capacity, in number of Items allowed
     */
    protected abstract int capacity();

    /**
     * Get the item decay rate, which differs in difference subclass, and can be
     * changed in run time by the user, so not a constant.
     *
     * @return The number of times for a decay factor to be fully applied
     */
    protected abstract int forgetRate();

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
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
        float f = (float) mass / (size() * TOTAL_LEVEL);
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
    public boolean contains(E it) {
        return nameTable.containsValue(it);
    }

    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    public E get(String key) {
        return nameTable.get(key);
    }

    /**
     * Add a new Item into the Bag
     *
     * @param newItem The new Item
     * @return Whether the new Item is added into the Bag
     */
    public boolean putIn(final E newItem) {
        final String newKey = newItem.getKey();
        final E oldItem = nameTable.put(newKey, newItem);
        if (oldItem != null) {                  // merge duplications
            outOfBase(oldItem);
            newItem.merge(oldItem);
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
     * Put an item back into the itemTable
     * <p>
     * The only place where the forgetting rate is applied
     *
     * @param oldItem The Item to put back
     * @return Whether the new Item is added into the Bag
     */
    public boolean putBack(E oldItem) {
        BudgetFunctions.forget(oldItem.getBudget(), forgetRate(), RELATIVE_THRESHOLD);
        return putIn(oldItem);
    }

    /**
     * Choose an Item according to priority distribution and take it out of the
     * Bag
     *
     * @return The selected Item
     */
    public E takeOut() {
        if (nameTable.isEmpty()) { // empty bag
            return null;
        }
        if (itemTableEmpty[currentLevel] || (currentCounter == 0)) { // done with the current level
            
            // look for a non-empty level
            do {
                currentLevel = DISTRIBUTOR.order[levelIndex];
                levelIndex = (levelIndex + 1) % DISTRIBUTOR.capacity;
            } while (itemTableEmpty[currentLevel]);
            
            if (currentLevel < THRESHOLD) { // for dormant levels, take one item
                currentCounter = 1;
            } else {                  // for active levels, take all current items
                currentCounter = itemTable[currentLevel].size();
            }
        }
        final E selected = takeOutFirst(currentLevel); // take out the first item in the level
        currentCounter--;
        nameTable.remove(selected.getKey());
        refresh();
        return selected;
    }

    /**
     * Pick an item by key, then remove it from the bag
     *
     * @param key The given key
     * @return The Item with the key
     */
    public E pickOut(String key) {
        E picked = nameTable.get(key);
        if (picked != null) {
            outOfBase(picked);
            nameTable.remove(key);
        }
        return picked;
    }

    /**
     * Check whether a level is empty
     *
     * @param n The level index
     * @return Whether that level is empty
     */
    public boolean emptyLevel(final int n) {
        return itemTable[n].isEmpty();
    }

    /**
     * Decide the put-in level according to priority
     *
     * @param item The Item to put in
     * @return The put-in level
     */
    private int getLevel(final E item) {
        final float fl = item.getPriority() * TOTAL_LEVEL;
        final int level = (int) Math.ceil(fl) - 1;
        return (level < 0) ? 0 : level;     // cannot be -1
    }

    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param newItem The Item to put in
     * @return The overflow Item
     */
    private E intoBase(E newItem) {
        E oldItem = null;
        int inLevel = getLevel(newItem);
        if (size() > capacity) {      // the bag is full
            int outLevel = 0;
            //while (itemTable[outLevel].isEmpty()) {
            while (itemTableEmpty[outLevel]) {
                outLevel++;
            }
            if (outLevel > inLevel) {           // ignore the item and exit
                return newItem;
            } else {                            // remove an old item in the lowest non-empty level
                oldItem = takeOutFirst(outLevel);
            }
        }
        itemTable[inLevel].add(newItem);        // FIFO
        itemTableEmpty[inLevel] = false;
        mass += (inLevel + 1);                  // increase total mass
        refresh();                              // refresh the window
        return oldItem;		// TODO return null is a bad smell
    }

    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    private E takeOutFirst(final int level) {
        final E selected = itemTable[level].get(0);
        itemTable[level].remove(0);
        if (itemTable[level].isEmpty())
            itemTableEmpty[level] = true;
        mass -= (level + 1);
        refresh();
        return selected;
    }

    /**
     * Remove an item from itemTable, then adjust mass
     *
     * @param oldItem The Item to be removed
     */
    protected void outOfBase(final E oldItem) {
        final int level = getLevel(oldItem);
        itemTable[level].remove(oldItem);
        if (itemTable[level].isEmpty())
            itemTableEmpty[level] = true;
        mass -= (level + 1);
        refresh();
    }

    /**
     * To start displaying the Bag in a BagWindow; {@link nars.gui.BagWindow}
     * implements interface {@link BagObserver};
     *
     * @param bagObserver BagObserver to set
     * @param title The title of the window
     */
    public void addBagObserver(BagObserver<E> bagObserver, String title) {
        this.bagObserver = bagObserver;
        bagObserver.post(toString());
        bagObserver.setTitle(title);
        bagObserver.setBag(this);
    }

    /**
     * Resume display
     */
    public void play() {
        if (bagObserver!=null)
            bagObserver.post(toString());
    }

    /**
     * Stop display
     */
    public void stop() {
        if (bagObserver!=null)        
            bagObserver.stop();
    }

    /**
     * Refresh display
     */
    public void refresh() {
        if (bagObserver!=null)       
            if (bagObserver.isActive()) {
                bagObserver.refresh(toString());
            }
    }

    /**
     * Collect Bag content into a String for display
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(" ");
        for (int i = TOTAL_LEVEL; i >= showLevel; i--) {
            if (!itemTable[i-1].isEmpty()) {
                buf = buf.append("\n --- Level ").append(i).append(":\n ");
                for (int j = 0; j < itemTable[i - 1].size(); j++) {
                    buf = buf.append(itemTable[i - 1].get(j).toStringBrief()).append("\n ");
                }
            }
        }
        return buf.toString();
    }

    /**
     * TODO refactor : paste from preceding method
     */
    public String toStringLong() {
        StringBuffer buf = new StringBuffer(" BAG " + getClass().getSimpleName());
        buf.append(" ").append(showSizes());
        for (int i = TOTAL_LEVEL; i >= showLevel; i--) {
            if (!itemTable[i-1].isEmpty()) {
                buf = buf.append("\n --- LEVEL ").append(i).append(":\n ");
                for (int j = 0; j < itemTable[i - 1].size(); j++) {
                    buf = buf.append(itemTable[i - 1].get(j).toStringLong()).append("\n ");
                }
            }
        }
        buf.append(">>>> end of Bag").append(getClass().getSimpleName());
        return buf.toString();
    }

    /**
     * show item Table Sizes
     */
    String showSizes() {
        StringBuilder buf = new StringBuilder(" ");
        int levels = 0;
        for (ArrayList<E> items : itemTable) {
            if ((items != null) && !items.isEmpty()) {
                levels++;
                buf.append(items.size()).append(" ");
            }
        }
        return "Levels: " + Integer.toString(levels) + ", sizes: " + buf;
    }

    /**
     * set Show Level
     */
    public void setShowLevel(int showLevel) {
        this.showLevel = showLevel;
    }

    public int getMass() {
        return mass;
    }
    
}
