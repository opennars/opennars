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
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.entity;

import nars.core.Parameters;
import nars.inference.BudgetFunctions;
import static nars.inference.UtilityFunctions.and;
import static nars.inference.UtilityFunctions.aveGeo;
import static nars.inference.UtilityFunctions.or;
import nars.io.Symbols;

/**
 * A triple of priority (current), durability (decay), and quality (long-term average).
 */
public class BudgetValue implements Cloneable {

    /** The character that marks the two ends of a budget value */
    private static final char MARK = Symbols.BUDGET_VALUE_MARK;
    /** The character that separates the factors in a budget value */
    private static final char SEPARATOR = Symbols.VALUE_SEPARATOR;
   
    
    /** The relative share of time resource to be allocated */
    final public ShortFloat priority;
    
    /**
     * The percent of priority to be kept in a constant period; All priority
     * values "decay" over time, though at different rates. Each item is given a
     * "durability" factor in (0, 1) to specify the percentage of priority level
     * left after each reevaluation
     */
    final public ShortFloat durability;
    
    /** The overall (context-independent) evaluation */
    final public ShortFloat quality;

    /** 
     * Default constructor
     */
    public BudgetValue() {
        priority = new ShortFloat(0.01f);
        durability = new ShortFloat(0.01f);
        quality = new ShortFloat(0.01f);
    }

    /** 
     * Constructor with initialization
     * @param p Initial priority
     * @param d Initial durability
     * @param q Initial quality
     */
    public BudgetValue(final float p, final float d, final float q) {
        priority = new ShortFloat(p);
        durability = new ShortFloat(d);
        quality = new ShortFloat(q);
    }

    /**
     * Cloning constructor
     * @param v Budget value to be cloned
     */
    public BudgetValue(final BudgetValue v) {
        priority = new ShortFloat(v.getPriority());
        durability = new ShortFloat(v.getDurability());
        quality = new ShortFloat(v.getQuality());
    }

    /**
     * Cloning method
     */
    @Override
    public Object clone() {
        return new BudgetValue(this.getPriority(), this.getDurability(), this.getQuality());
    }

    /**
     * Get priority value
     * @return The current priority
     */
    public float getPriority() {
        return priority.getValue();
    }
    public short getPriorityShort() {
        return priority.getShortValue();
    }

    /**
     * Change priority value
     * @param v The new priority
     */
    public void setPriority(final float v) {
        priority.setValue(v);
    }

    /**
     * Increase priority value by a percentage of the remaining range
     * @param v The increasing percent
     */
    public void incPriority(final float v) {
        priority.setValue(or(priority.getValue(), v));
    }

    /**
     * Decrease priority value by a percentage of the remaining range
     * @param v The decreasing percent
     */
    public void decPriority(final float v) {
        priority.setValue(and(priority.getValue(), v));
    }

    /**
     * Get durability value
     * @return The current durability
     */
    public float getDurability() {
        return durability.getValue();
    }

    /**
     * Change durability value
     * @param v The new durability
     */
    public void setDurability(final float v) {
        durability.setValue(v);
    }

    /**
     * Increase durability value by a percentage of the remaining range
     * @param v The increasing percent
     */
    public void incDurability(final float v) {
        durability.setValue(or(durability.getValue(), v));
    }

    /**
     * Decrease durability value by a percentage of the remaining range
     * @param v The decreasing percent
     */
    public void decDurability(final float v) {
        durability.setValue(and(durability.getValue(), v));
    }

    /**
     * Get quality value
     * @return The current quality
     */
    public float getQuality() {
        return quality.getValue();
    }

    /**
     * Change quality value
     * @param v The new quality
     */
    public void setQuality(final float v) {
        quality.setValue(v);
    }

    /**
     * Increase quality value by a percentage of the remaining range
     * @param v The increasing percent
     */
    public void incQuality(final float v) {
        quality.setValue(or(quality.getValue(), v));
    }

    /**
     * Decrease quality value by a percentage of the remaining range
     * @param v The decreasing percent
     */
    public void decQuality(final float v) {
        quality.setValue(and(quality.getValue(), v));
    }

    /**
     * Merge one BudgetValue into another
     * @param that The other Budget
     */
    public void merge(final BudgetValue that) {
        BudgetFunctions.merge(this, that);
    }

    /**
     * To summarize a BudgetValue into a single number in [0, 1]
     * @return The summary value
     */
    public float summary() {
        return aveGeo(priority.getValue(), durability.getValue(), quality.getValue());
    }

    /**
     * Whether the budget should get any processing at all
     * <p>
     * to be revised to depend on how busy the system is
     * @return The decision on whether to process the Item
     */
    public boolean aboveThreshold() {
        return (summary() >= Parameters.BUDGET_THRESHOLD);
    }

    /**
     * Fully display the BudgetValue
     * @return String representation of the value
     */
    @Override
    public String toString() {
        
        
        return MARK + priority.toString() + SEPARATOR + durability.toString() + SEPARATOR + quality.toString() + MARK;
    }

    /**
     * Briefly display the BudgetValue
     * @return String representation of the value with 2-digit accuracy
     */
    public String toStringBrief() {
        //return MARK + priority.toStringBrief() + SEPARATOR + durability.toStringBrief() + SEPARATOR + quality.toStringBrief() + MARK;

        final String priorityString = priority.toStringBrief();
        final String durabilityString = durability.toStringBrief();
        final String qualityString = quality.toStringBrief();
        return new StringBuilder(1 + priorityString.length() + 1 + durabilityString.length() + 1 + qualityString.length() + 1)
            .append(MARK)
            .append(priorityString).append(SEPARATOR)
            .append(durabilityString).append(SEPARATOR)
            .append(qualityString)
            .append(MARK)
            .toString();                
    }
}
