/*
 * Item.java
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

package com.googlecode.opennars.entity;

import com.googlecode.opennars.main.Memory;

/**
 * An item is an object that can be put into a Bag,
 * and it participates in the resource competation of the system.
 */
public abstract class Item extends BudgetValue implements Cloneable {

	/**
     * The key of the Item, unique in a Bag
     */
    protected String key;   // uniquely define an Item in a bag
    
    protected Item(Memory memory) {
    	super(memory);
    }
    
    protected Item(BudgetValue v, Memory memory) {
        super(v, memory);
    }
            
    /**
     * Get the current key
     * @return Current key value
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Set a new key value
     * @param k New key value
     */
    public void setKey(String k) {
        key = k;
    }

    /**
     * Get current BudgetValue
     * @return Current BudgetValue
     */
    public BudgetValue getBudget() {
        return this;
    }

    /**
     * Set new BudgetValue
     * @param v new BudgetValue
     */
    public void setBudget(BudgetValue v) {      // is this necessary?
        setPriority(v.getPriority());
        setDurability(v.getDurability());
        setQuality(v.getQuality());
    }
}
