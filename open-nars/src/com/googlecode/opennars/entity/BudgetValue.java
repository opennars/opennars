/*
 * BudgetValue.java
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

import com.googlecode.opennars.inference.*;
import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.*;

/**
 * A triple of priority (current), durability (decay), and quality (long-term average).
 */
public class BudgetValue implements Cloneable {
    public static final char mark = Symbols.BUDGET_VALUE_MARK;       // default
    public static final char separator = Symbols.VALUE_SEPARATOR;    // default
    protected ShortFloat priority;      // short-term, first-order
    protected ShortFloat durability;    // short-term, second-order
    protected ShortFloat quality;       // long-term
	private Memory memory;
	protected BudgetFunctions budgetfunctions;
    
    /* ----- constructors ----- */
    
    public BudgetValue(Memory memory) {
        priority = new ShortFloat(0.01f);
        durability = new ShortFloat(0.01f);
        quality = new ShortFloat(0.01f);
        this.memory = memory;
        this.budgetfunctions = new BudgetFunctions(memory);
    }

    public BudgetValue(float p, float d, float q, Memory memory) {
        priority = new ShortFloat(p);
        durability = new ShortFloat(d);
        quality = new ShortFloat(q);
        this.memory = memory;
        this.budgetfunctions = new BudgetFunctions(memory);
    }

    public BudgetValue(BudgetValue v, Memory memory) { // clone, not copy
        priority = new ShortFloat(v.getPriority());
        durability = new ShortFloat(v.getDurability());
        quality = new ShortFloat(v.getQuality());
        this.memory = memory;
        this.budgetfunctions = new BudgetFunctions(memory);
    }

    public Object clone() {
        return new BudgetValue(this.getPriority(), this.getDurability(), this.getQuality(), this.memory);
    }

    /* ----- priority ----- */

    public float getPriority() {
        return priority.getValue();
    }

    public void setPriority(float v) {
        priority.setValue(v);
    }
    
    public void incPriority(float v) {
        priority.setValue(UtilityFunctions.or(priority.getValue(), v));
    }

    public void decPriority(float v) {
        priority.setValue(UtilityFunctions.and(priority.getValue(), v));
    }

    /* ----- durability ----- */

    public float getDurability() {
        return durability.getValue();
    }
    
    public void setDurability(float v) {
        durability.setValue(v);
    }
    
    public void incDurability(float v) {
        durability.setValue(UtilityFunctions.or(durability.getValue(), v));
    }

    public void decDurability(float v) {
        durability.setValue(UtilityFunctions.and(durability.getValue(), v));
    }

    /* ----- quality ----- */

    public float getQuality() {
        return quality.getValue();
    }
    
    public void setQuality(float v) {
        quality.setValue(v);
    }
        
    public void incQuality(float v) {
        quality.setValue(UtilityFunctions.or(quality.getValue(), v));
    }

    public void decQuality(float v) {
        quality.setValue(UtilityFunctions.and(quality.getValue(), v));
    }

    /* ----- utility ----- */

    public void merge(BudgetValue that) {
        this.budgetfunctions.merge(this, that);
    }

    // called from Memory only
    public float singleValue() {
//        return (priority.getValue() + quality.getValue()) * durability.getValue() / 2.0f;
        return UtilityFunctions.aveAri(priority.getValue(), durability.getValue(), quality.getValue());
    }
    
    public boolean aboveThreshold() {
        return (singleValue() > 0.001); // to be revised to depend on how busy the system is
    }
    
    // full output
    public String toString() {
        return mark + priority.toString() + separator + durability.toString() + separator + quality.toString() + mark;
    }

    // short output
    public String toString2() {
        return mark + priority.toString2() + separator + durability.toString2() + separator + quality.toString2() + mark;
    }

	/**
	 * @param memory the memory to set
	 */
	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	/**
	 * @return the memory
	 */
	public Memory getMemory() {
		return memory;
	}
}
