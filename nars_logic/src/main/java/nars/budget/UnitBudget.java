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
package nars.budget;

import nars.nal.nal7.Tense;
import nars.truth.Truth;
import nars.util.data.Util;

import static nars.nal.UtilityFunctions.and;
import static nars.nal.UtilityFunctions.or;

/**
 * A triple of priority (current), durability (decay), and quality (long-term average).
 *
 * Mutable, unit-scaled (1.0 max) budget value
 *
 */
public class UnitBudget extends Budget {



    //common instance for a 'deleted budget'.  TODO use a wrapper class to make it unmodifiable
    public static final Budget deleted = new UnitBudget();
    static {  deleted.delete(); }

    //common instance for a 'zero budget'.  TODO use a wrapper class to make it unmodifiable
    public static final Budget zero = new UnitBudget();
    static {  zero.zero();    }

    /**
     * The relative share of time resource to be allocated
     */
    private float priority;

    /**
     * The percent of priority to be kept in a constant period; All priority
     * values "decay" over time, though at different rates. Each item is given a
     * "durability" factor in (0, 1) to specify the percentage of priority level
     * left after each reevaluation
     */
    private float durability;

    /**
     * The overall (context-independent) evaluation
     */
    private float quality;


    /**
     * time at which this budget was last forgotten, for calculating accurate memory decay rates
     */
    protected long lastForgetTime = Tense.TIMELESS;


    public UnitBudget(float p, float d, Truth qualityFromTruth) {
        this(p, d, qualityFromTruth !=
                null ? BudgetFunctions.truthToQuality(qualityFromTruth) : 1.0f);
    }


    /**
     * Constructor with initialization
     *
     * @param p Initial priority
     * @param d Initial durability
     * @param q Initial quality
     */
    public UnitBudget(float p, float d, float q) {
        setPriority(p);
        setDurability(d);
        setQuality(q);
    }


    /**
     * begins with 0.0f for all components
     */
    public UnitBudget() {
    }

    public UnitBudget(Budget v) {
        this(v, false);
    }

    /**
     * Cloning constructor
     *
     * @param v Budget value to be cloned
     */
    public UnitBudget(Budget v, boolean copyLastForgetTime) {
        this();
        if (v != null) {
            budget(v);
            if (!copyLastForgetTime)
                setLastForgetTime(-1);
        }
    }





    /**
     * Cloning method
     * TODO give this a less amgiuous name to avoid conflict with subclasses that have clone methods
     */
    @Override
    public final Budget clone() {
        return new UnitBudget(this, true);
    }




    /**
     * Get priority value
     *
     * @return The current priority
     */
    @Override
    public final float getPriority() {
        return priority;
    }

    /**
     * Change priority value
     *
     * @param p The new priority
     * @return whether the operation had any effect
     */
    @Override
    public final void setPriority(float p) {
        if (Budget.getDeleted(p)) {
            throw new RuntimeException("NaN priority");
        }
        priority = Util.clamp(p);
    }

    @Override
    public void deleteBudget() {
        this.priority = Float.NaN;
    }

    /**
     * Get durability value
     *
     * @return The current durability
     */
    @Override
    public final float getDurability() {
        return durability;
    }

    /**
     * Change durability value
     *
     * @param d The new durability
     */
    @Override
    public final void setDurability(float d) {
        durability = Util.clamp(d);

    }



    /**
     * Get quality value
     *
     * @return The current quality
     */
    @Override
    public final float getQuality() {
        return quality;
    }

    /**
     * Change quality value
     *
     * @param q The new quality
     */
    @Override
    public final void setQuality(float q) {
        quality = Util.clamp(q);
    }

    /**
     * Increase quality value by a percentage of the remaining range
     *
     * @param v The increasing percent
     */
    public void orQuality(float v) {
        quality = or(quality, v);
    }

    /**
     * Decrease quality value by a percentage of the remaining range
     *
     * @param v The decreasing percent
     */
    public void andQuality(float v) {
        quality = and(quality, v);
    }


    public boolean equals(Object that) {
        throw new RuntimeException("N/A");
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("N/A");
     }


    /**
     * Fully display the BudgetValue
     *
     * @return String representation of the value
     */
    @Override
    public String toString() {
        return getBudgetString();
    }




    @Override
    public final long setLastForgetTime(long currentTime) {

        long period = lastForgetTime == Tense.TIMELESS ? 0 : currentTime - lastForgetTime;

        lastForgetTime = currentTime;
        return period;
    }

    @Override
    public long getLastForgetTime() {
        return lastForgetTime;
    }





    @Override
    public void mulPriority(float factor) {
        setPriority(getPriority() * factor);
    }

    public void mulDurability(float factor) {
        setDurability(getDurability() * factor);
    }



}
