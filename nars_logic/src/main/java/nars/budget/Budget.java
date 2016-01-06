package nars.budget;

import nars.Symbols;
import nars.data.BudgetedStruct;
import nars.util.Texts;

import javax.annotation.Nullable;

import static java.lang.Math.pow;
import static nars.nal.UtilityFunctions.and;
import static nars.nal.UtilityFunctions.or;
import static nars.util.data.Util.equal;
import static nars.util.data.Util.mean;

/**
 * Created by me on 12/11/15.
 */
public abstract class Budget extends BudgetedHandle {


    public static final BudgetMerge plus = (tgt, src, srcScale) -> {
        float dp = src.getPriority() * srcScale;

        float currentPriority = tgt.getPriorityIfNaNThenZero();

        float nextPri = currentPriority + dp;
        if (nextPri > 1) nextPri = 1f;

        float currentNextPrioritySum = currentPriority + nextPri;

        /* current proportion */
        float cp;
        cp = currentNextPrioritySum != 0 ? currentPriority / currentNextPrioritySum : 0.5f;

        /* next proportion = 1 - cp */
        float np = 1.0f - cp;

        float nextDur = cp * tgt.getDurability() + np * src.getDurability();
        float nextQua = cp * tgt.getQuality() + np * src.getQuality();

        assert !Float.isNaN(nextDur) : "NaN dur: " + src + ' ' + tgt.getDurability();
        assert !Float.isNaN(nextQua) : "NaN quality";

        tgt.budget( nextPri,nextDur,nextQua);
    };

    //@Contract(pure = true)
    public static boolean aveGeoNotLessThan(float min, float a, float b, float c) {
        float minCubed = min * min * min; //cube both sides
        return a * b * c >= minCubed;
    }

    public static float aveGeo(float a, float b, float c) {
        return (float) pow(a * b * c, 1.0 / 3.0);
    }

    //@Contract(pure = true)
    public static boolean getDeleted(float pri) {
        return Float.isNaN(pri);
    }

    public static String toString(Budget b) {

        return toStringBuilder(new StringBuilder(), Texts.n4(b.getPriority()), Texts.n4(b.getDurability()), Texts.n4(b.getQuality())).toString();
    }

    public static StringBuilder toStringBuilder(StringBuilder sb, CharSequence priorityString, CharSequence durabilityString, CharSequence qualityString) {
        int c = 1 + priorityString.length() + 1 + durabilityString.length() + 1 + qualityString.length() + 1;
        if (sb == null)
            sb = new StringBuilder(c);
        else
            sb.ensureCapacity(c);

        sb.append(Symbols.BUDGET_VALUE_MARK)
                .append(priorityString).append(Symbols.VALUE_SEPARATOR)
                .append(durabilityString).append(Symbols.VALUE_SEPARATOR)
                .append(qualityString)
                .append(Symbols.BUDGET_VALUE_MARK);

        return  sb;
    }

    /**
     * set all quantities to zero
     */
    public Budget zero() {
        return budget(0, 0, 0);
    }

    public void delete() {
        deleteBudget();
    }

    public void deleteBudget() {
        setPriority(Float.NaN);
    }

    
    
    public Budget getBudget() {
        return this;
    }

    
    public abstract float getPriority();

    
    public abstract void setPriority(float p);

    /**
     * returns the period in time: currentTime - lastForgetTime and sets the lastForgetTime to currentTime
     */

    public abstract long setLastForgetTime(long currentTime);

    
    public abstract long getLastForgetTime();

    public void mulPriority(float factor) {
        setPriority(getPriority() * factor);
    }

    
    public abstract float getDurability();

    public abstract void setDurability(float d);

    
    public abstract float getQuality();

    public abstract void setQuality(float q);

    public boolean equalsByPrecision(Budget t, float epsilon) {
        return equal(getPriority(), t.getPriority(), epsilon) &&
                equal(getDurability(), t.getDurability(), epsilon) &&
                equal(getQuality(), t.getQuality(), epsilon);
    }

    /**
     * Increase priority value by a percentage of the remaining range.
     * Uses the 'or' function so it is not linear
     *
     * @param v The increasing percent
     */
    public void orPriority(float v) {
        setPriority(or(getPriority(), v));
    }

    /**
     * merges another budget into this one, averaging each component
     */
    public void mergeAverage(Budget that) {
        if (this == that) return;

        budget(
                mean(getPriority(), that.getPriority()),
                mean(getDurability(), that.getDurability()),
                mean(getQuality(), that.getQuality())
        );
    }


    /**
     * To summarize a BudgetValue into a single number in [0, 1]
     *
     * @return The summary value
     */
    public float summary() {
        return aveGeo(getPriority(), getDurability(), getQuality());
    }

    public abstract Budget clone();

    public boolean summaryLessThan(float s) {
        return !summaryNotLessThan(s);
    }

    /**
     * uses optimized aveGeoNotLessThan to avoid a cube root operation
     */
    public boolean summaryNotLessThan(float min) {
        return min == 0f || aveGeoNotLessThan(min, getPriority(), getDurability(), getQuality());
    }


//    public void maxDurability(final float otherDurability) {
//        setDurability(Util.max(getDurability(), otherDurability)); //max durab
//    }
//
//    public void maxQuality(final float otherQuality) {
//        setQuality(Util.max(getQuality(), otherQuality)); //max durab
//    }

    /**
     * Increase durability value by a percentage of the remaining range
     *
     * @param v The increasing percent
     */
    public void orDurability(float v) {
        setDurability(or(getDurability(), v));
    }

    /**
     * Decrease durability value by a percentage of the remaining range
     *
     * @param v The decreasing percent
     */
    public void andDurability(float v) {
        setDurability(and(getDurability(), v));
    }

    /**
     * AND's (multiplies) priority with another value
     */
    public void andPriority(float v) {
        setPriority(and(getPriority(), v));
    }

    /**
     * Whether the budget should get any processing at all
     * <p>
     * to be revised to depend on how busy the system is
     * tests whether summary >= threhsold
     *
     * @return The decision on whether to process the Item
     */
    public boolean summaryGreaterOrEqual(float budgetThreshold) {

        if (getDeleted()) return false;

        /* since budget can only be positive.. */
        if (budgetThreshold <= 0) return true;


        return summaryNotLessThan(budgetThreshold);
    }

    /**
     * copies a budget into this; if source is null, it deletes the budget
     */
    public BudgetedStruct budget(@Nullable Budget source) {
        if (source == null) {
            zero();
        } else {
            budget(source.getPriority(), source.getDurability(), source.getQuality());
            setLastForgetTime(source.getLastForgetTime());
        }

        return this;
    }

    /**
     * returns this budget, after being modified
     */
    public Budget budget(float p, float d, float q) {
        setPriority(p);
        setDurability(d);
        setQuality(q);
        return this;
    }

    public BudgetedStruct budget(BudgetedHandle source) {
        return budget(source.getBudget());
    }

    public float getPriorityIfNaNThenZero() {
        return !Float.isNaN(getPriority()) ? getPriority() : 0;
    }

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

        CharSequence priorityString = Texts.n2(getPriority());
        CharSequence durabilityString = Texts.n2(getDurability());
        CharSequence qualityString = Texts.n2(getQuality());

        return toStringBuilder(sb, priorityString, durabilityString, qualityString);
    }

    public String toBudgetString() {
        return toBudgetStringExternal().toString();
    }

    public String getBudgetString() {
        return toString(this);
    }

    public void set(Budget b) {
        budget(b.getPriority(), b.getDurability(), b.getQuality());
    }

}
