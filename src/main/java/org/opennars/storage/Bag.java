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
import org.opennars.inference.BudgetFunctions;

import java.util.Iterator;

public abstract class Bag<E extends Item<K>,K> implements Iterable<E> {

    public abstract void clear();   
    
    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    abstract public E get(final K key);

    /**
     * Get the max. amount of items the Bag can store
     * @return capacity 
     */
    abstract public int getCapacity();

    /**
     * Choose an Item according to distribution policy
     * @return a item with taking it from the bag or null if this bag is empty
     */
    abstract public E takeNext();
    
    
    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param newItem The Item to put in
     * @return The overflow Item, or null if nothing displaced
     */
    protected abstract E addItem(final E newItem);

    /**
     * Add a new Item into the Bag
     *
     * @param newItem The new Item
     * @return the item which was removed, which may be the input item if it could not be inserted; or null if nothing needed removed
     */
    public E putIn(E newItem) {
        final K newKey = newItem.name();
        final E existingItemWithSameKey = take(newKey);
        if (existingItemWithSameKey != null) {            
            newItem = (E)existingItemWithSameKey.merge(newItem);
        }
        // put the (new or merged) item into itemTable        
        final E overflowItem = addItem(newItem);
        if (overflowItem!=null) {
            return overflowItem;
        }            
        else {
            return null;
        }
    }

    /**
     * Removes an item by key
     * 
     * @param key The key
     * @return the removed item
     */
    abstract public E take(final K key);

    /**
     * Removes item by value 
     * @param value
     * @return the removed item's name
     */
    public E take(final E value) {
        return take(value.name());
    }
    
    
    /**
     * @return The number of items in the bag
     */
    public abstract int size();

    /**
     * @return average of the priority of all items
     */
    public abstract float getAveragePriority();

    /**
     * @return iterator for all items in (approximately) descending priority
     */
    @Override public abstract Iterator<E> iterator();
    
    /**
     * Put an item back into the itemTable
     * <p>
     * The only place where the forgetting rate is applied
     *
     * @param oldItem The Item to put back
     * @param m related memory
     * @return the item which was removed, or null if none removed
     */    
    public E putBack(final E oldItem, final float forgetCycles, final Memory m) {
        final float relativeThreshold = m.narParameters.QUALITY_RESCALED;
        BudgetFunctions.applyForgetting(oldItem.budget, forgetCycles, relativeThreshold);
        return putIn(oldItem);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();// + "(" + size() + "/" + getCapacity() +")";
    }    
}
