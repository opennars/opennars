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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.storage;

import java.util.*;

import com.googlecode.opennars.entity.Item;
import com.googlecode.opennars.inference.BudgetFunctions;
import com.googlecode.opennars.main.Parameters;


/**
 * A Bag is a storage with a constant capacity and maintains an internal priority
 * distribution for retrieval.
 * 
 * Each entity in a bag must extend Item, which is a UsageValue with a key.
 *
 * A name table is used to merge duplicate items.
 *
 * The bag space is divided by a threshold, above which is mainly time management,
 *   and below, space mamagement.
 *   Differences: (1) level selection vs. item selection, (2) decay rate
 */
public abstract class Bag<Type extends Item> {
    
    protected static final int TOTAL_LEVEL = Parameters.BAG_LEVEL;          // priority levels
    protected static final int THRESHOLD = Parameters.BAG_THRESHOLD;        // firing threshold
    protected static final float RELATIVE_THRESHOLD = (float) THRESHOLD / (float) TOTAL_LEVEL;
    protected static final float LOAD_FACTOR = Parameters.LOAD_FACTOR;       // hashtable parameter
    protected static final Distributor DISTRIBUTOR = new Distributor(TOTAL_LEVEL); // only one instance

    protected HashMap<String, Type> nameTable;    // from key to item
    protected ArrayList<Type> itemTable[];        // array of lists of items, for items on different level
       
    protected int capacity;         // defined in different bags
    protected int mass;             // current sum of occupied level
    protected int levelIndex;       // index to get next level, kept in individual objects
    protected int currentLevel;     // current take out level
    protected int currentCounter;   // maximum number of items to be taken out at current level
    
    
    // ---------- constructor ---------- //
    
    // called from subclasses
    protected Bag() {
        capacity = capacity();
        levelIndex = capacity % TOTAL_LEVEL; // so that different bags start at different point
        currentLevel = TOTAL_LEVEL - 1;
        itemTable = new ArrayList[TOTAL_LEVEL];
        nameTable = new HashMap<String, Type>((int) (capacity / LOAD_FACTOR), LOAD_FACTOR);
    }
    
    // --- property methods --- //
    
    protected abstract int capacity();
    
    // the number of times for a decay factor to be fully applied
    // it can be changed in run time by the user, so not a constant
    protected abstract int forgetRate();
    
    // --- Bag property --- //
    
    public ArrayList<Type> getAllContents() {
    	return (ArrayList<Type>) this.nameTable.values();
    }
    
    // get the average priority of the bag --- can be removed???
    public float averagePriority() {
        if (nameTable.size() == 0)
            return 0.01f;
        float f = (float) mass / (nameTable.size() * TOTAL_LEVEL);
        if (f > 1)
            return 1.0f;
        return f;
    }
    
    // check if an item is in the bag
    public boolean contains(Type it) {
        return nameTable.containsValue(it);
    }

    public Type get(String key) {
        return nameTable.get(key);
    }
    
    // ---------- put/remove methods ---------- //
    
    // put a new Item into the Bag
    public void putIn(Type newItem) {
        String newKey = newItem.getKey();
        Type oldItem = nameTable.put(newKey, newItem);
        if (oldItem != null) {                  // merge duplications
            outOfBase(oldItem);
            newItem.merge(oldItem);
        }
        Type overflowItem = intoBase(newItem);  // put the (new or merged) item into itemTable
        if (overflowItem != null) {             // remove overflow
            String overflowKey = overflowItem.getKey();
            nameTable.remove(overflowKey);
        }
    }
    
    // put an item back into the itemTable (it is already in the table)
    public void putBack(Type oldItem) {
        BudgetFunctions.forget(oldItem.getBudget(), forgetRate(), RELATIVE_THRESHOLD);
        putIn(oldItem);
    }
    
    // choose an item according to priority distribution
    // and take it out of the itemTable
    public Type takeOut() {     // default behavior: the Item will be putBack
        if (mass == 0)                              // empty bag
            return null;
        if (emptyLevel(currentLevel) || (currentCounter == 0)) { // done with the current level
            currentLevel = DISTRIBUTOR.pick(levelIndex);
            levelIndex = DISTRIBUTOR.next(levelIndex);
            while (emptyLevel(currentLevel)) {          // look for a non-empty level
                currentLevel = DISTRIBUTOR.pick(levelIndex);
                levelIndex = DISTRIBUTOR.next(levelIndex);
            }
            if (currentLevel < THRESHOLD)
                currentCounter = 1;                             // for dormant levels, take one item
            else
                currentCounter = itemTable[currentLevel].size();     // for active levels, take all current items
        }
        Type selected = takeOutFirst(currentLevel); // take out the first item in the level
        currentCounter--;
        nameTable.remove(selected.getKey());    // 01-07-04
        return selected;
    }
    
    // pick an item by key, then remove it from the bag
    public Type pickOut(String key) {
        Type picked = nameTable.get(key);
        if (picked != null) {
            outOfBase(picked);
            nameTable.remove(key);    // 01-07-04
        }
        return picked;
    }
    
    // ---------- private methods ---------- //
    
    // check whether a level is empty
    private boolean emptyLevel(int n) {
        if (itemTable[n] == null)
            return true;
        else
            return itemTable[n].isEmpty();
    }
    
    // decide the in level according to priority
    private int getLevel(Type item) {
        float fl = item.getPriority() * TOTAL_LEVEL;
        int level = (int) Math.ceil(fl) - 1;
        return (level < 0) ? 0 : level;     // cannot be -1
    }
    
    // insert an item into the itemTable, and return the overflow
    private Type intoBase(Type newItem) {
        Type oldItem = null;
        int inLevel = getLevel(newItem);
        if (nameTable.size() > capacity) {      // the bag is full
            int outLevel = 0;
            while (emptyLevel(outLevel))
                outLevel++;
            if (outLevel > inLevel) {           // ignore the item and exit
                return newItem;
            } else {                            // remove an old item in the lowest non-empty level
                oldItem = takeOutFirst(outLevel);
            }
        }
        if (itemTable[inLevel] == null)
            itemTable[inLevel] = new ArrayList();               // first time insert
        itemTable[inLevel].add(newItem);          // FIFO
        mass += (inLevel + 1);                          // increase total mass
        return oldItem;
    }
    
    // take out the first or last Type in a level from the itemTable
    private Type takeOutFirst(int level) {
        Type selected = itemTable[level].get(0);
        itemTable[level].remove(0);         // take the item out
        mass -= (level + 1);                    // decrease total mass
        return selected;
    }
    
    // remove an item from itemTable, then adjust mass
    protected void outOfBase(Type oldItem) {
        int level = getLevel(oldItem);
        itemTable[level].remove(oldItem);
        mass -= (level + 1);                    // decrease total mass
    }
}
