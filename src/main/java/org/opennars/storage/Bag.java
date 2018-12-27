/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.storage;

import org.opennars.entity.Item;
import java.io.Serializable;
import java.util.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.main.Parameters;

/**
 * Original Bag implementation which distributes items into
 * discrete levels (queues) according to priority
 */
public class Bag<Type extends Item<K>,K> implements Serializable, Iterable<Type>  {
    
       /** priority levels */
    private final int TOTAL_LEVEL;
    /** firing threshold */
    private final int THRESHOLD;
    /** relative threshold, only calculate once */
    private final float RELATIVE_THRESHOLD;
    /** shared DISTRIBUTOR that produce the probability distribution */
    private Distributor DISTRIBUTOR;
    /** mapping from key to item */
    
    private HashMap<K, Type> nameTable;
    /** array of lists of items, for items on different level */
    private ArrayList<ArrayList<Type>> itemTable;
    /** defined in different bags */
    private int capacity;
    /** current sum of occupied level */
    private int mass;
    /** index to get next level, kept in individual objects */
    private int levelIndex;
    /** current take out level */
    private int currentLevel;
    /** maximum number of items to be taken out at current level */
    private int currentCounter;
    
    public Bag(final int levels, final int capacity, Parameters narParameters) {
        this(levels, capacity, (int) (narParameters.BAG_THRESHOLD * levels));
    }

    /** thresholdLevel = 0 disables "fire level completely" threshold effect */
    public Bag(final int levels, final int capacity, final int thresholdLevel) {
        this.TOTAL_LEVEL = levels;
        DISTRIBUTOR = new Distributor(TOTAL_LEVEL); 
        this.THRESHOLD = thresholdLevel;
        this.RELATIVE_THRESHOLD = (float) THRESHOLD / (float) TOTAL_LEVEL;
        this.capacity = capacity;
        clear();
    }
    
     public void clear() {
        itemTable = new ArrayList<ArrayList<Type>>(TOTAL_LEVEL);
        for (int i = 0; i < TOTAL_LEVEL; i++) {
            itemTable.add(new ArrayList<Type>());
        }
        nameTable = new HashMap<K, Type>();
        currentLevel = TOTAL_LEVEL - 1;
        levelIndex = capacity % TOTAL_LEVEL; // so that different bags start at different point
        mass = 0;
        currentCounter = 0;
    }

    /**
     * Get the average priority of Items
     * @return The average priority of Items in the bag
     */
    public float getAveragePriority() {
        if (nameTable.isEmpty()) {
            return 0.01f;
        }
        float f = (float) mass / (nameTable.size() * TOTAL_LEVEL);
        if (f > 1) {
            return 1.0f;
        }
        return f;
    }

    /**
     * Check if an item is in the bag
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    public boolean contains(Type it) {
        return nameTable.containsValue(it);
    }

    /**
     * Get an Item by key
     * @param key The key of the Item
     * @return The Item with the given key
     */
    public Type get(K key) {
        return nameTable.get(key);
    }

    /**
     * Add a new Item into the Bag
     * @param newItem The new Item
     * @return Whether the new Item is added into the Bag
     */
    public Type putIn(Type newItem) {
        K newKey = newItem.name();
        Type oldItem = nameTable.put(newKey, newItem);
        if (oldItem != null) {                  // merge duplications
            outOfBase(oldItem);
            newItem.merge(oldItem);
        }
        Type overflowItem = intoBase(newItem);  // put the (new or merged) item into itemTable
        if (overflowItem != null) {             // remove overflow
            K overflowKey = overflowItem.name();
            nameTable.remove(overflowKey);
            return overflowItem;
        } else {
            return null;
        }
    }
    
    /**
     * Put an item back into the itemTable
     * <p>
     * The only place where the forgetting rate is applied
     *
     * @param oldItem The Item to put back
     * @param m related memory
     * @return the item which was removed, or null if none removed
     */    
    public Type putBack(final Type oldItem, final float forgetCycles, final Memory m) {
        final float relativeThreshold = m.narParameters.QUALITY_RESCALED;
        BudgetFunctions.applyForgetting(oldItem.budget, forgetCycles, relativeThreshold);
        return putIn(oldItem);
    }

    /**
     * Choose an Item according to priority distribution and take it out of the Bag
     * @return The selected Item
     */
    public Type takeOut() {
        if (nameTable.isEmpty()) { // empty bag
            return null;
        }
        if (emptyLevel(currentLevel) || (currentCounter == 0)) { // done with the current level
            currentLevel = DISTRIBUTOR.pick(levelIndex);
            levelIndex = DISTRIBUTOR.next(levelIndex);
            while (emptyLevel(currentLevel)) {          // look for a non-empty level
                currentLevel = DISTRIBUTOR.pick(levelIndex);
                levelIndex = DISTRIBUTOR.next(levelIndex);
            }
            if (currentLevel < THRESHOLD) { // for dormant levels, take one item
                currentCounter = 1;
            } else {                  // for active levels, take all current items
                currentCounter = itemTable.get(currentLevel).size();
            }
        }
        Type selected = takeOutFirst(currentLevel); // take out the first item in the level
        currentCounter--;
        nameTable.remove(selected.name());
        return selected;
    }

    /**
     * Pick an item by key, then remove it from the bag
     * @param key The given key
     * @return The Item with the key
     */
    public Type pickOut(K key) {
        Type picked = nameTable.get(key);
        if (picked != null) {
            outOfBase(picked);
            nameTable.remove(key);
        }
        return picked;
    }
    public Type pickOut(Type val) {
        return pickOut(val.name());
    }

    /**
     * Check whether a level is empty
     * @param n The level index
     * @return Whether that level is empty
     */
    protected boolean emptyLevel(int n) {
        return ((itemTable.get(n) == null) || itemTable.get(n).isEmpty());
    }

    /**
     * Decide the put-in level according to priority
     * @param item The Item to put in
     * @return The put-in level
     */
    private int getLevel(Type item) {
        float fl = item.getPriority() * TOTAL_LEVEL;
        int level = (int) Math.ceil(fl) - 1;
        return (level < 0) ? 0 : level;     // cannot be -1
    }

    /**
     * Insert an item into the itemTable, and return the overflow
     * @param newItem The Item to put in
     * @return The overflow Item
     */
    private Type intoBase(Type newItem) {
        Type oldItem = null;
        int inLevel = getLevel(newItem);
        if (nameTable.size() > capacity) {      // the bag is full
            int outLevel = 0;
            while (emptyLevel(outLevel)) {
                outLevel++;
            }
            if (outLevel > inLevel) {           // ignore the item and exit
                return newItem;
            } else {                            // remove an old item in the lowest non-empty level
                oldItem = takeOutFirst(outLevel);
            }
        }
        itemTable.get(inLevel).add(newItem);        // FIFO
        mass += (inLevel + 1);                  // increase total mass
        return oldItem;		// TODO return null is a bad smell
    }

    /**
     * Take out the first or last Type in a level from the itemTable
     * @param level The current level
     * @return The first Item
     */
    private Type takeOutFirst(int level) {
        Type selected = itemTable.get(level).get(0);
        itemTable.get(level).remove(0);
        mass -= (level + 1);
        return selected;
    }

    /**
     * Remove an item from itemTable, then adjust mass
     * @param oldItem The Item to be removed
     */
    protected void outOfBase(Type oldItem) {
        int level = getLevel(oldItem);
        itemTable.get(level).remove(oldItem);
        mass -= (level + 1);
    }

    /**
     * Collect Bag content into a String for display
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(" ");
	for (int i = TOTAL_LEVEL; i >= 0 ; i--) {
            if (!emptyLevel(i - 1)) {
                buf = buf.append("\n --- Level " + i + ":\n ");
                for (int j = 0; j < itemTable.get(i - 1).size(); j++) {
                    buf = buf.append(itemTable.get(i - 1).get(j).toString() + "\n ");
                }
            }
        }
        return buf.toString();
    }
    
    /** TODO bad paste from preceding */
    public String toStringLong() {
        StringBuffer buf = new StringBuffer(" BAG " + getClass().getSimpleName() );
        buf.append(" ").append( showSizes() );
		for (int i = TOTAL_LEVEL; i >= 0; i--) {
            if (!emptyLevel(i - 1)) {
                buf = buf.append("\n --- LEVEL " + i + ":\n ");
                for (int j = 0; j < itemTable.get(i - 1).size(); j++) {
                    buf = buf.append(itemTable.get(i - 1).get(j).toStringLong() + "\n ");
                }
            }
        }
		buf.append(">>>> end of Bag").append( getClass().getSimpleName() );
        return buf.toString();
    }
    
    String showSizes() {
        StringBuilder buf = new StringBuilder(" ");
    	int levels = 0;
    	for ( ArrayList<Type> items : itemTable) {
            if ((items != null) && ! items.isEmpty()) {
				levels++;
				buf.append( items.size() ).append( " " );
            }
		}
    	return "Levels: " + Integer.toString( levels ) + ", sizes: " + buf;
    }
    
    public int size() { 
        return nameTable.size();
    }

    @Override
    public Iterator<Type> iterator() {
        return nameTable.values().iterator();
    }
}
