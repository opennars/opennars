/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.entity;

import org.opennars.inference.BudgetFunctions;
import org.opennars.io.Symbols;
import org.opennars.io.Texts;

import java.io.Serializable;

import static org.opennars.inference.UtilityFunctions.*;
import org.opennars.main.Parameters;
/**
 * A triple of priority (current), durability (decay), and quality (long-term average).
 */
public class BudgetValue implements Cloneable, Serializable {

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

    /** time at which this budget was last forgotten, for calculating accurate memory decay rates */
    private long lastForgetTime = -1;
    
    private Parameters narParameters;
    public BudgetValue(final float p, final float d, final TruthValue qualityFromTruth, Parameters narParameters) {
        this(p, d, BudgetFunctions.truthToQuality(qualityFromTruth), narParameters);
    }


    /** 
     * Constructor with initialization
     * @param p Initial priority
     * @param d Initial durability
     * @param q Initial quality
     */
    public BudgetValue(final float p, final float d, final float q, Parameters narParameters) {
        this.narParameters = narParameters;
        priority = p;
        durability = d;
        quality = q;
        
        if(d>=1.0) {
            durability=(float) (1.0-narParameters.TRUTH_EPSILON);
            //throw new IllegalStateException("durability value above or equal 1");
        }
        if(p>1.0) {
            priority=1.0f;
            //throw new IllegalStateException("priority value above 1");
        }
    }

    /**
     * Cloning constructor
     * @param v Budget value to be cloned
     */
    public BudgetValue(final BudgetValue v) {
        this(v.getPriority(), v.getDurability(), v.getQuality(), v.narParameters);
    }

    /**
     * Cloning method
     */
    @Override
    public BudgetValue clone() {
        return new BudgetValue(this.getPriority(), this.getDurability(), this.getQuality(), this.narParameters);
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
    public final void setPriority(final float v) {
        if(v>1.0f) {
            throw new IllegalStateException("Priority > 1.0: " + v);
            //v=1.0f;
        }
        priority = v;
    }

    /**
     * Increase priority value by a percentage of the remaining range
     * @param v The increasing percent
     */
    public void incPriority(final float v) {        
        setPriority( (float) Math.min(1.0, or(priority, v)));
    }

    /** AND's (multiplies) priority with another value */
    public void andPriority(final float v) {
        setPriority( and(priority, v) );
    }

    /**
     * Decrease priority value by a percentage of the remaining range
     * @param v The decreasing percent
     */
    public void decPriority(final float v) {
        setPriority( and(priority, v) );
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
     * @param d The new durability
     */
    public void setDurability(float d) {
        if(d>=1.0f) {
            d=1.0f-this.narParameters.TRUTH_EPSILON;
        }
        durability = d;
    }

    /**
     * Increase durability value by a percentage of the remaining range
     * @param v The increasing percent
     */
    public void incDurability(final float v) {
        float durability2 = or(durability, v);
        if(durability2>=1.0f) {
            durability2=1.0f-this.narParameters.TRUTH_EPSILON; //put into allowed range
        }
        durability=durability2;
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
     * returns true if this budget is greater in all quantities than another budget,
     * used to prevent a merge that would have no consequence
     * @param other
     * @return 
     */
    public boolean greaterThan(final BudgetValue other) {
        return (getPriority() - other.getPriority() > this.narParameters.BUDGET_THRESHOLD) &&
                (getDurability()- other.getDurability()> this.narParameters.BUDGET_THRESHOLD) &&
                (getQuality() - other.getQuality() > this.narParameters.BUDGET_THRESHOLD);
    }

    
    /**
     * To summarize a BudgetValue into a single number in [0, 1]
     * @return The summary value
     */
    public float summary() {
        return aveGeo(priority, durability, quality);
    }

    
    public boolean equalsByPrecision(final Object that) { 
        if (that instanceof BudgetValue) {
            final BudgetValue t = ((BudgetValue) that);
            final float dPrio = Math.abs(getPriority() - t.getPriority());
            if (dPrio >= this.narParameters.TRUTH_EPSILON) return false;
            final float dDura = Math.abs(getDurability() - t.getDurability());
            if (dDura >= this.narParameters.TRUTH_EPSILON) return false;
            final float dQual = Math.abs(getQuality() - t.getQuality());
            return dQual < this.narParameters.TRUTH_EPSILON;
        }
        return false;
    }

    
    /**
     * Whether the budget should get any processing at all
     * <p>
     * to be revised to depend on how busy the system is
     * @return The decision on whether to process the Item
     */
    public boolean aboveThreshold() {
        return (summary() >= this.narParameters.BUDGET_THRESHOLD);
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

        final CharSequence priorityString = Texts.n2(priority);
        final CharSequence durabilityString = Texts.n2(durability);
        final CharSequence qualityString = Texts.n2(quality);
        return new StringBuilder(1 + priorityString.length() + 1 + durabilityString.length() + 1 + qualityString.length() + 1)
            .append(MARK)
            .append(priorityString).append(SEPARATOR)
            .append(durabilityString).append(SEPARATOR)
            .append(qualityString)
            .append(MARK)
            .toString();                
    }

    /** returns the period in time: currentTime - lastForgetTime and sets the lastForgetTime to currentTime */
    public long setLastForgetTime(final long currentTime) {
        final long period;
        if (this.lastForgetTime == -1)            
            period = 0;
        else
            period = currentTime - lastForgetTime;
        
        lastForgetTime = currentTime;
        
        return period;
    }

    public long getLastForgetTime() {
        return lastForgetTime;
    }
}
