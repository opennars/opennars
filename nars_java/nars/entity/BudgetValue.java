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
import nars.io.Texts;

/**
 * A triple of priority (current), durability (decay), and quality (long-term average).
 */
public class BudgetValue implements Cloneable {

    /** The character that marks the two ends of a budget value */
    private static final char MARK = Symbols.BUDGET_VALUE_MARK;
    /** The character that separates the factors in a budget value */
    private static final char SEPARATOR = Symbols.VALUE_SEPARATOR;
   
    
    /** The relative share of time resource to be allocated */
    private float priority;
    
    /**
     * The percent of priority to be kept in a constant period; All priority
     * values "decay" over time, though at different rates. Each item is given a
     * "durability" factor in (0, 1) to specify the percentage of priority level
     * left after each reevaluation
     */
    private float durability;
    
    /** The overall (context-independent) evaluation */
    private float quality;

    /** 
     * Default constructor
     */
    public BudgetValue() {
        priority = 0.01f;
        durability = 0.01f;
        quality = 0.01f;
    }

    /** 
     * Constructor with initialization
     * @param p Initial priority
     * @param d Initial durability
     * @param q Initial quality
     */
    public BudgetValue(final float p, final float d, final float q) {
        priority = p;
        durability = d;
        quality = q;
    }

    /**
     * Cloning constructor
     * @param v Budget value to be cloned
     */
    public BudgetValue(final BudgetValue v) {
        priority = v.getPriority();
        durability = v.getDurability();
        quality = v.getQuality();
    }

    /**
     * Cloning method
     */
    @Override
    public BudgetValue clone() {
        return new BudgetValue(this.getPriority(), this.getDurability(), this.getQuality());
    }

    /**
     * Get priority value
     * @return The current priority
     */
    public float getPriority() {
        return priority;
    }

    /**
     * Change priority value
     * @param v The new priority
     */
    public void setPriority(final float v) {
        priority = v;
    }

    /**
     * Increase priority value by a percentage of the remaining range
     * @param v The increasing percent
     */
    public void incPriority(final float v) {
        priority = or(priority, v);
    }

    /** AND's (multiplies) priority with another value */
    public void andPriority(final float v) {
        priority = and(priority, v);
    }

    /**
     * Decrease priority value by a percentage of the remaining range
     * @param v The decreasing percent
     */
    public void decPriority(final float v) {
        priority = and(priority, v);
    }

    /**
     * Get durability value
     * @return The current durability
     */
    public float getDurability() {
        return durability;
    }

    /**
     * Change durability value
     * @param v The new durability
     */
    public void setDurability(final float d) {
        durability = d;
    }

    /**
     * Increase durability value by a percentage of the remaining range
     * @param v The increasing percent
     */
    public void incDurability(final float v) {
        durability = or(durability, v);
    }

    /**
     * Decrease durability value by a percentage of the remaining range
     * @param v The decreasing percent
     */
    public void decDurability(final float v) {
        durability = and(durability, v);
    }

    /**
     * Get quality value
     * @return The current quality
     */
    public float getQuality() {
        return quality;
    }

    /**
     * Change quality value
     * @param v The new quality
     */
    public void setQuality(final float v) {
        quality = v;
    }

    /**
     * Increase quality value by a percentage of the remaining range
     * @param v The increasing percent
     */
    public void incQuality(final float v) {
        quality = or(quality, v);
    }

    /**
     * Decrease quality value by a percentage of the remaining range
     * @param v The decreasing percent
     */
    public void decQuality(final float v) {
        quality = and(quality, v);
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
        return aveGeo(priority, durability, quality);
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
        return MARK + Texts.n4(priority) + SEPARATOR + Texts.n4(durability) + SEPARATOR + Texts.n4(quality) + MARK;
    }

    /**
     * Briefly display the BudgetValue
     * @return String representation of the value with 2-digit accuracy
     */
    public String toStringExternal() {
        //return MARK + priority.toStringBrief() + SEPARATOR + durability.toStringBrief() + SEPARATOR + quality.toStringBrief() + MARK;

        final String priorityString = Texts.n2(priority);
        final String durabilityString = Texts.n2(durability);
        final String qualityString = Texts.n2(quality);
        return new StringBuilder(1 + priorityString.length() + 1 + durabilityString.length() + 1 + qualityString.length() + 1)
            .append(MARK)
            .append(priorityString).append(SEPARATOR)
            .append(durabilityString).append(SEPARATOR)
            .append(qualityString)
            .append(MARK)
            .toString();                
    }

    /**
     * linear interpolate the priority value to another value
     * @see https://en.wikipedia.org/wiki/Linear_interpolation
     */
    public void lerpPriority(final float targetValue, final float momentum) {
        setPriority( (getPriority() * momentum) + ((1f - momentum) * targetValue) );
    }

}
