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

import com.google.common.util.concurrent.AtomicDouble;
import com.gs.collections.api.block.procedure.Procedure2;
import nars.Global;
import nars.Symbols;
import nars.io.Texts;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.truth.Truth;
import nars.util.data.Util;
import org.apache.commons.math3.util.FastMath;

import javax.annotation.Nullable;
import java.io.Serializable;

import static nars.Global.BUDGET_EPSILON;
import static nars.nal.UtilityFunctions.*;

/**
 * A triple of priority (current), durability (decay), and quality (long-term average).
 * TODO abstract/subclasses
 * interface Budgetable
 * ForgettableBudgetValue - includes the last forget time
 * some subclasses / adapter classes for statistics,
 * monitoring or event notification on changes
 */
public class Budget implements Cloneable, Prioritized, Serializable {

    public static final Procedure2<Budget,Budget> average =
            //(Procedure2<Budget, Budget>) Budget::mergeAverage;
            (Procedure2<Budget, Budget>) Budget::mergeAverageLERP;

    public static final Procedure2<Budget,Budget> plus = (Procedure2<Budget, Budget>) Budget::mergePlus;
    public static final Procedure2<Budget,Budget> max = (Procedure2<Budget, Budget>) Budget::mergeMax;



    //common instance for a 'deleted budget'.  TODO use a wrapper class to make it unmodifiable
    public static Budget deleted = new Budget();
    static {  deleted.delete(); }

    //common instance for a 'zero budget'.  TODO use a wrapper class to make it unmodifiable
    public static Budget zero = new Budget();
    static {  zero.zero();    }


    /**
     * The relative share of time resource to be allocated
     */
    protected float priority;

    /**
     * The percent of priority to be kept in a constant period; All priority
     * values "decay" over time, though at different rates. Each item is given a
     * "durability" factor in (0, 1) to specify the percentage of priority level
     * left after each reevaluation
     */
    protected float durability;

    /**
     * The overall (context-independent) evaluation
     */
    protected float quality;

    /**
     * time at which this budget was last forgotten, for calculating accurate memory decay rates
     */
    long lastForgetTime = Stamp.TIMELESS;

    public Budget(char punctuation, Truth qualityFromTruth) {
        this(punctuation == Symbols.JUDGMENT ? Global.DEFAULT_JUDGMENT_PRIORITY :
                        (punctuation == Symbols.QUESTION ? Global.DEFAULT_QUESTION_PRIORITY :
                                (punctuation == Symbols.GOAL ? Global.DEFAULT_GOAL_PRIORITY :
                                        Global.DEFAULT_QUEST_PRIORITY)),
                punctuation, qualityFromTruth);
    }

    public Budget(final float p, char punctuation, Truth qualityFromTruth) {
        this(p,
                punctuation == Symbols.JUDGMENT ? Global.DEFAULT_JUDGMENT_DURABILITY :
                        (punctuation == Symbols.QUESTION ? Global.DEFAULT_QUESTION_DURABILITY :
                                (punctuation == Symbols.GOAL ? Global.DEFAULT_GOAL_DURABILITY :
                                        Global.DEFAULT_QUEST_DURABILITY)),
                qualityFromTruth);
    }

    public Budget(final float p, final float d, final Truth qualityFromTruth) {
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
    public Budget(final float p, final float d, final float q) {

        //if (requireValidStoredValues()) {
            if (Float.isNaN(p) || Float.isNaN(d) || Float.isNaN(q))
                throw new RuntimeException("non-finite values: " + p + "," + q + "," + q);
        //}

        setPriority(p);
        setDurability(d);
        setQuality(q);
    }


    /**
     * begins with 0.0f for all components
     */
    public Budget() {
        super();
    }

    /**
     * Cloning constructor
     *
     * @param v Budget value to be cloned
     */
    public Budget(final Budget v, boolean copyLastForgetTime) {
        this();
        if (v != null) {
            budget(v);
            if (!copyLastForgetTime)
                setLastForgetTime(-1);
        }
    }

    /*
    public static void ensureBetweenZeroAndOne(final float v) {
        String err = null;
        if (Float.isNaN(v)) err = ("value is NaN");
        else if (v > 1.0f)  err = ("value > 1.0: " + v);
        else if (v < 0.0f)  err = ("value < 1.0: " + v);

        if (err!=null)
            throw new RuntimeException(err);
    }*/


//    /**
//     * creates a new Budget instance, should be avoided if possible
//     */
//    final public static Budget budgetIfAboveThreshold(final float budgetThreshold, final float pri, final float dur, final float qua) {
//        if (aveGeoNotLessThan(budgetThreshold, pri, dur, qua))
//            return new Budget(pri, dur, qua);
//        return null;
//    }

    public static String toString(final Budget b) {
        //return MARK + Texts.n4(b.getPriority()) + SEPARATOR + Texts.n4(b.getDurability()) + SEPARATOR + Texts.n4(b.getQuality()) + MARK;
        return Budget.toStringBuilder(new StringBuilder(), Texts.n4(b.priority), Texts.n4(b.durability), Texts.n4(b.quality)).toString();
    }

    private static StringBuilder toStringBuilder(StringBuilder sb, final CharSequence priorityString, final CharSequence durabilityString, final CharSequence qualityString) {
        final int c = 1 + priorityString.length() + 1 + durabilityString.length() + 1 + qualityString.length() + 1;
        if (sb == null)
            sb = new StringBuilder(c);
        else
            sb.ensureCapacity(c);

        sb.append(Symbols.BUDGET_VALUE_MARK)
                .append(priorityString).append(Symbols.VALUE_SEPARATOR)
                .append(durabilityString).append(Symbols.VALUE_SEPARATOR)
                .append(qualityString)
                .append(Symbols.BUDGET_VALUE_MARK);

        return sb;
    }


    public static float newDefaultPriority(char punctuation) {
        switch (punctuation) {
            case Symbols.JUDGMENT:
                return Global.DEFAULT_JUDGMENT_PRIORITY;

            case Symbols.QUEST:
            case Symbols.QUESTION:
                return Global.DEFAULT_QUESTION_PRIORITY;

            case Symbols.GOAL:
                return Global.DEFAULT_GOAL_PRIORITY;
        }
        throw new RuntimeException("Unknown sentence type: " + punctuation);
    }

    public static float newDefaultDurability(char punctuation) {
        switch (punctuation) {
            case Symbols.JUDGMENT:
                return Global.DEFAULT_JUDGMENT_DURABILITY;
            case Symbols.QUEST:
            case Symbols.QUESTION:
                return Global.DEFAULT_QUESTION_DURABILITY;
            case Symbols.GOAL:
                return Global.DEFAULT_GOAL_DURABILITY;
        }
        throw new RuntimeException("Unknown sentence type: " + punctuation);
    }

    public static float summarySum(Iterable<? extends Budgeted> dd) {
        float f = 0;
        for (final Budgeted x : dd)
            f += x.getBudget().summary();
        return f;
    }

    public static float prioritySum(Iterable<? extends Budgeted> dd) {
        float f = 0;
        for (final Budgeted x : dd)
            f += x.getPriority();
        return f;
    }


    /**
     * Cloning method
     * TODO give this a less amgiuous name to avoid conflict with subclasses that have clone methods
     */
    @Override
    public final Budget clone() {
        return new Budget(this, true);
    }

    /** the max priority, durability, and quality of two tasks */
    public final Budget mergeMax(final Budget b) {
        return budget(
                Util.max(getPriority(), b.getPriority()),
                Util.max(getDurability(), b.getDurability()),
                Util.max(getQuality(), b.getQuality())
        );
    }

    /**
     * priority: adds the value of another budgetvalue to this; all components max at 1.0
     * durability: max(this, b) (similar to merge)
     * quality: max(this, b)    (similar to merge)
     */
    public final Budget mergePlus(final Budget b) {
        return mergePlus(b, 1f);
    }

    public final Budget mergePlus(final Budget b, float factor) {
        return mergePlus(b.getPriority(), b.getDurability(), b.getQuality(), factor);
    }

    public final Budget mergePlus(float addPriority, final float otherDurability, final float otherQuality) {
        return mergePlus(addPriority, otherDurability, otherQuality, 1f);
    }

    /** linearly interpolates the change affected to determine dur, qua */
    final Budget mergePlus(final float addPriority, final float otherDurability, final float otherQuality, float factor) {

        final float dp = addPriority * factor;

        final float currentPriority = getPriority();

        final float nextPriority = FastMath.min(1,currentPriority + dp);

        final float currentNextPrioritySum = (currentPriority + nextPriority);

        /* current proportion */ final float cp = (Util.equal(currentNextPrioritySum, 0, BUDGET_EPSILON)) ?
                0.5f : /* both are zero so they have equal infleunce */
                (currentPriority / currentNextPrioritySum);
        /* next proportion */ final float np = 1f - cp;

        return budget(
                nextPriority,
                (cp * getDurability()) + (np * otherDurability),
                (cp * getQuality()) + (np * otherQuality)
        );
    }

    /**
     * merges another budget into this one, averaging each component
     */
    public void mergeAverageLERP(final Budget that) {
        if (this == that) return;

        final float currentPriority = getPriority();

        final float otherPriority = that.getPriority();

        final float prisum = (currentPriority + otherPriority);

        /* current proportion */
        final float cp = (Util.equal(prisum, 0, BUDGET_EPSILON)) ?
                0.5f : /* both are zero so they have equal infleunce */
                (currentPriority / prisum);

        /* next proportion */
        final float np = 1f - cp;

        budget(
                cp * getPriority() + np * that.getPriority(),
                cp * getDurability() + np * that.getDurability(),
                cp * getQuality() + np * that.getQuality()
        );
    }

//    public final Budget accumulateLerp(final float addPriority, final float otherDurability, final float otherQuality) {
//        final float currentPriority = getPriority();
//
//        /** influence factor determining LERP amount of blending durability and quality */
//        final float f = 1 - Math.abs(currentPriority - clamp(addPriority+currentPriority));
//
//        return set(
//                currentPriority + addPriority,
//                lerp(getDurability(), otherDurability, f),
//                lerp(getQuality(), otherQuality, f)
//                //max(getDurability(), otherDurability),
//                //max(getQuality(), otherQuality)
//        );
//    }


    /**
     * set all quantities to zero
     */
    public final Budget zero() {
        this.priority = this.durability = this.quality = 0f;
        return this;
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
    public final void setPriority(final float p) {
        if (isDeleted(p)) {
            throw new RuntimeException("NaN priority");
        }
        this.priority = Util.clamp(p);
    }

    /**
     * Increase priority value by a percentage of the remaining range.
     * Uses the 'or' function so it is not linear
     *
     * @param v The increasing percent
     */
    public void orPriority(final float v) {
        setPriority( or(priority, v) );
    }

//    public void maxDurability(final float otherDurability) {
//        setDurability(Util.max(getDurability(), otherDurability)); //max durab
//    }
//
//    public void maxQuality(final float otherQuality) {
//        setQuality(Util.max(getQuality(), otherQuality)); //max durab
//    }

    /**
     * AND's (multiplies) priority with another value
     */
    public void andPriority(final float v) {
        setPriority(and(getPriority(), v));
    }



    /**
     * Get durability value
     *
     * @return The current durability
     */
    public final float getDurability() {
        return durability;
    }

    /**
     * Change durability value
     *
     * @param d The new durability
     */
    public final void setDurability(final float d) {
        this.durability = Util.clamp(d);
    }

    /**
     * Increase durability value by a percentage of the remaining range
     *
     * @param v The increasing percent
     */
    public final void orDurability(final float v) {
        setDurability(or(durability, v));
    }

    /**
     * Decrease durability value by a percentage of the remaining range
     *
     * @param v The decreasing percent
     */
    public final void andDurability(final float v) {
        setDurability(and(durability, v));
    }


//    /**
//     * returns true if this budget is greater in all quantities than another budget,
//     * used to prevent a merge that would have no consequence
//     * NOT TESTED
//     * @param other
//     * @return
//     */
//    public boolean greaterThan(final BudgetValue other) {
//        return (getPriority() - other.getPriority() > Parameters.BUDGET_THRESHOLD) &&
//                (getDurability()- other.getDurability()> Parameters.BUDGET_THRESHOLD) &&
//                (getQuality() - other.getQuality() > Parameters.BUDGET_THRESHOLD);
//    }

    /**
     * Get quality value
     *
     * @return The current quality
     */
    public float getQuality() {
        return quality;
    }

    /**
     * Change quality value
     *
     * @param q The new quality
     */
    public final void setQuality(final float q) {
        this.quality = Util.clamp(q);
    }

//    public float summary(float additionalPriority) {
//        return aveGeo(Math.min(1.0f, priority + additionalPriority), durability, quality);
//    }

    /**
     * Increase quality value by a percentage of the remaining range
     *
     * @param v The increasing percent
     */
    public void orQuality(final float v) {
        quality = or(quality, v);
    }

    /**
     * Decrease quality value by a percentage of the remaining range
     *
     * @param v The decreasing percent
     */
    public void andQuality(final float v) {
        quality = and(quality, v);
    }

//    /**
//     * Merge one BudgetValue into another
//     *
//     * @param that The other Budget
//     * @return whether the merge had any effect
//     */
//    @Override
//    public void merge(final Prioritized that) {
//        setPriority(mean(getPriority(), that.getPriority()));
//    }

    /**
     * merges another budget into this one, averaging each component
     */
    public void mergeAverage(final Budget that) {
        if (this == that) return;

        budget(
                mean(getPriority(), that.getPriority()),
                mean(getDurability(), that.getDurability()),
                mean(getQuality(), that.getQuality())
        );
    }


//    /**
//     * applies a merge only if the changes would be significant
//     * (the difference in value equal to or exceeding the budget epsilon parameter)
//     *
//     * @return whether change occurred
//     */
//    public boolean mergeIfChanges(Budget target, float budgetEpsilon) {
//        if (this == target) return false;
//
//        final float p = mean(getPriority(), target.getPriority());
//        final float d = mean(getDurability(), target.getDurability());
//        final float q = mean(getQuality(), target.getQuality());
//
//        return setIfChanges(p, d, q, budgetEpsilon);
//    }

    /**
     * To summarize a BudgetValue into a single number in [0, 1]
     *
     * @return The summary value
     */
    public float summary() {
        return aveGeo(priority, durability, quality);
    }

//    /* Whether budget is above threshold, with the involvement of additional priority (saved previously, or boosting)
//     * @param additionalPriority saved credit to contribute to possibly push it over threshold
//     */
//    public boolean aboveThreshold(float additionalPriority) {
//        return (summary(additionalPriority) >= Global.BUDGET_THRESHOLD);
//    }

    /**
     * uses optimized aveGeoNotLessThan to avoid a cube root operation
     */
    public boolean summaryNotLessThan(final float min) {
        return aveGeoNotLessThan(min, priority, durability, quality);
    }

//    /**
//     * Whether budget is above threshold, with the involvement of additional priority (saved previously, or boosting)
//     * @param additionalPriority
//     * @return NaN if neither aboveThreshold, nor aboveThreshold with additional priority; 0 if no additional priority necessary to make above threshold, > 0 if that amount of the additional priority was "spent" to cause it to go above threshold
//     */
//    public float aboveThreshold(float additionalPriority) {
//        float s = summary();
//        if (s >= Parameters.BUDGET_THRESHOLD)
//            return 0;
//        if (summary(additionalPriority) >= Parameters.BUDGET_EPSILON) {
//            //calculate how much was necessary
//
//            float dT = Parameters.BUDGET_THRESHOLD - s; //difference between how much needed
//
//            //TODO solve for additional:
//            //  newSummary - s = dT
//            //  ((priority+additional)*(duration)*(quality))^(1/3) - s = dT;
//
//            float used = 0;
//        }
//        return Float.NaN;
//    }

    public final boolean equalsByPrecision(final Budget t) {
        return equalsByPrecision(t, BUDGET_EPSILON);
    }

    public final boolean equalsByPrecision(final Budget t, final float epsilon) {
        return  equal(getPriority(), t.getPriority(), epsilon) &&
                equal(getDurability(), t.getDurability(), epsilon) &&
                equal(getQuality(), t.getQuality(), epsilon);
    }

    public final boolean equalsBudget(final Budget t) {
        return equalsByPrecision(t) && (getLastForgetTime() == t.getLastForgetTime());
    }

    public boolean equals(final Object that) {
        if (that instanceof Budget) {
            return equalsBudget((Budget) that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("N/A");
        //this will be relatively slow if used in a hash collection
        //TODO if hashes are needed, it should use a similar hashing method as Truth values does
        //return Objects.hash(getPriority(), getDurability(), getQuality());
    }

    /**
     * Whether the budget should get any processing at all
     * <p>
     * to be revised to depend on how busy the system is
     * tests whether summary >= threhsold
     *
     * @return The decision on whether to process the Item
     */
    final public boolean summaryGreaterOrEqual(final float budgetThreshold) {

        /* since budget can only be positive.. */
        if (budgetThreshold <= 0) return true;

        return summaryNotLessThan(budgetThreshold);
    }

    final public boolean summaryGreaterOrEqual(final AtomicDouble budgetThreshold) {
        return summaryGreaterOrEqual(budgetThreshold.floatValue());
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

    public String getBudgetString() {
        return Budget.toString(this);
    }

    /**
     * linear interpolate the priority value to another value
     * @see https://en.wikipedia.org/wiki/Linear_interpolation
     */
    /*public void lerpPriority(final float targetValue, final float momentum) {
        if (momentum == 1.0) 
            return;
        else if (momentum == 0) 
            setPriority(targetValue);
        else
            setPriority( (getPriority() * momentum) + ((1f - momentum) * targetValue) );
    }*/

    /**
     * Briefly display the BudgetValue
     *
     * @return String representation of the value with 2-digit accuracy
     */
    public StringBuilder toBudgetStringExternal() {
        return toBudgetStringExternal(null);
    }

    public StringBuilder toBudgetStringExternal(StringBuilder sb) {
        //return MARK + priority.toStringBrief() + SEPARATOR + durability.toStringBrief() + SEPARATOR + quality.toStringBrief() + MARK;

        final CharSequence priorityString = Texts.n2(priority);
        final CharSequence durabilityString = Texts.n2(durability);
        final CharSequence qualityString = Texts.n2(quality);

        return toStringBuilder(sb, priorityString, durabilityString, qualityString);
    }

    public String toBudgetString() {
        return toBudgetStringExternal().toString();
    }

    /**
     * 1 digit resolution
     */
    public String toStringExternalBudget1(boolean includeQuality) {
        final char priorityString = Texts.n1char(priority);
        final char durabilityString = Texts.n1char(durability);
        StringBuilder sb = new StringBuilder(1 + 1 + 1 + (includeQuality ? 1 : 0) + 1)
                .append(Symbols.BUDGET_VALUE_MARK)
                .append(priorityString).append(Symbols.VALUE_SEPARATOR)
                .append(durabilityString);

        if (includeQuality)
            sb.append(Symbols.VALUE_SEPARATOR).append(Texts.n1char(quality));

        return sb.append(Symbols.BUDGET_VALUE_MARK).toString();
    }

    /**
     * returns the period in time: currentTime - lastForgetTime and sets the lastForgetTime to currentTime
     */
    @Override
    final public long setLastForgetTime(final long currentTime) {

        final long period;

        if (this.lastForgetTime == Stamp.TIMELESS) {
            period = 0;
        }
        else {
            period = currentTime - lastForgetTime;
        }

        this.lastForgetTime = currentTime;
        return period;
    }

//    public Budget budget(final float p, final float d, final float q) {
//        setPriority(p);
//        setDurability(d);
//        setQuality(q);
//        return this;
//    }

    @Override
    public long getLastForgetTime() {
        return lastForgetTime;
    }

    /**
     * fast version which avoids bounds checking, safe to use if getting values from an existing Budget instance
     */
    protected final Budget budgetDirect(final float p, final float d, final float q) {

        if (isDeleted(p)) {
            throw new RuntimeException("source budget invalid");
        }

        this.priority = p;
        this.durability = d;
        this.quality = q;
        return this;
    }

    /** if source is null, it deletes the budget */
    public Budget budget(@Nullable final Budget source) {
        if (source == null) {
            zero();
        }
        else {
            budget(
                    source.getPriority(),
                    source.getDurability(),
                    source.getQuality());

            setLastForgetTime(source.getLastForgetTime());
        }

        return this;
    }

    /**
     * returns this budget, after being modified
     */
    protected Budget budget(final float p, final float d, final float q) {
        setPriority(p);
        setDurability(d);
        setQuality(q);
        return this;
    }

    final public float getPriorityIfNaNThenZero() {
        final float p;
        if (!Float.isNaN(p = getPriority()))
            return p;
        return 0;
    }


//    @Override
//    public float receive(float amount) {
//        float maxReceivable = 1.0f - getPriority();
//
//        float received = Math.min(amount, maxReceivable);
//        addPriority(received);
//
//        return amount - received;
//    }

//    /**
//     * modifies the budget if any of the components are signifiantly different
//     * returns whether the budget was changed
//     */
//    protected boolean setIfChanges(final float p, final float d, final float q, float budgetEpsilon) {
//        float dp = abs(getPriority() - p);
//        float dd = abs(getDurability() - d);
//        float dq = abs(getQuality() - q);
//
//        if (dp < budgetEpsilon && dd < budgetEpsilon && dq < budgetEpsilon)
//            return false;
//
//        set(p, d, q);
//        return true;
//    }

    @Override
    public void mulPriority(final float factor) {
        setPriority(getPriority() * factor);
    }

    public void mulDurability(final float factor) {
        setDurability(getDurability() * factor);
    }

    public final boolean summaryLessThan(final float s) {
        return !summaryNotLessThan(s);
    }

    final public Budget forget(final long now, final float forgetCycles, final float relativeThreshold) {
        //BudgetFunctions.forgetPeriodic(this, forgetCycles, relativeThreshold, now);
        BudgetFunctions.forgetAlann(this, forgetCycles, now);
        return this;
    }

    public boolean delete() {
        deleteBudget();
        return true;
    }

    public final void deleteBudget() {

        this.priority = Float.NaN;
        this.durability = 0; //trying a mix of NaN and 0's
        this.quality = 0;


                //Float.NaN, Float.NaN);
        //this.lastForgetTime = Stamp.TIMELESS;
    }

    public static final boolean isDeleted(float pri) {
        return Float.isNaN(pri);
    }

    public final boolean isDeleted() {
        return isDeleted(getPriority());
    }


    public static void requireNotDeleted(float pri) {
        if (isDeleted(pri)) {
            throw new RuntimeException("NaN priority");
        }

    }

    public static void requireNotDeleted(Task q) {
        requireNotDeleted(q.getPriority());
    }

}
