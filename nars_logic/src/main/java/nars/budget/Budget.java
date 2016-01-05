package nars.budget;

import nars.Symbols;
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
public interface Budget extends Budgeted {


    static boolean aveGeoNotLessThan(float min, float a, float b, float c) {
        float minCubed = min*min*min; //cube both sides
        return (a*b*c) >= minCubed;
    }

    static float aveGeo(float a, float b, float c) {
        return (float)pow(a*b*c, 1.0/3.0);
    }

    /**
     * set all quantities to zero
     */
    default Budget zero() {
        return budget(0,0,0);
    }

    default void delete() {
        deleteBudget();
    }

    default void deleteBudget() {
        setPriority(Float.NaN);
    }

    @Override
    default Budget getBudget() {
        return this;
    }

    @Override
    float getPriority();

    @Override
    void setPriority(float p);

    /**
     * returns the period in time: currentTime - lastForgetTime and sets the lastForgetTime to currentTime
     */
    @Override long setLastForgetTime(long currentTime);

    @Override
    long getLastForgetTime();

    @Override
    default void mulPriority(float factor) {
        setPriority(getPriority()*factor);
    }

    @Override
    float getDurability();

    void setDurability(float d);

    @Override
    float getQuality();

    void setQuality(float q);



    default boolean equalsByPrecision(Budget t, float epsilon) {
        return  equal(getPriority(), t.getPriority(), epsilon) &&
                equal(getDurability(), t.getDurability(), epsilon) &&
                equal(getQuality(), t.getQuality(), epsilon);
    }





    /**
     * Increase priority value by a percentage of the remaining range.
     * Uses the 'or' function so it is not linear
     *
     * @param v The increasing percent
     */
    default void orPriority(float v) {
        setPriority( or(getPriority(), v) );
    }



    /**
     * merges another budget into this one, averaging each component
     */
    default void mergeAverage(Budget that) {
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
    default float summary() {
        return aveGeo(getPriority(), getDurability(), getQuality());
    }

    Budget clone();

    static boolean isDeleted(float pri) {
        return Float.isNaN(pri);
    }

//    default boolean isDeleted() {
//        return AbstractBudget.isDeleted(getPriority());
//    }


    default boolean summaryLessThan(float s) {
        return !summaryNotLessThan(s);
    }

    /**
     * uses optimized aveGeoNotLessThan to avoid a cube root operation
     */
    default boolean summaryNotLessThan(float min) {
        if (min == 0f) return true;
        return aveGeoNotLessThan(min, getPriority(), getDurability(), getQuality());
    }

    /**
     * Increase durability value by a percentage of the remaining range
     *
     * @param v The increasing percent
     */
    default void orDurability(float v) {
        setDurability(or(getDurability(), v));
    }

    /**
     * Decrease durability value by a percentage of the remaining range
     *
     * @param v The decreasing percent
     */
    default void andDurability(float v) {
        setDurability(and(getDurability(), v));
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
    default void andPriority(float v) {
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
    default boolean summaryGreaterOrEqual(float budgetThreshold) {

        if (isDeleted()) return false;

        /* since budget can only be positive.. */
        if (budgetThreshold <= 0) return true;


        return summaryNotLessThan(budgetThreshold);
    }

//    default boolean summaryGreaterOrEqual(AtomicDouble budgetThreshold) {
//        return summaryGreaterOrEqual(budgetThreshold.floatValue());
//    }


    /** copies a budget into this; if source is null, it deletes the budget */
    default Budgeted budget(@Nullable Budget source) {
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
    default Budget budget(float p, float d, float q) {
        setPriority(p);
        setDurability(d);
        setQuality(q);
        return this;
    }

    default Budgeted budget(Budgeted source) {
        return budget(source.getBudget());
    }


    default float getPriorityIfNaNThenZero() {
        float p = getPriority();
        if (!Float.isNaN( p ))
            return p;
        return 0;
    }

    /**
     * Briefly display the BudgetValue
     *
     * @return String representation of the value with 2-digit accuracy
     */
    default StringBuilder toBudgetStringExternal() {
        return toBudgetStringExternal(null);
    }

    default StringBuilder toBudgetStringExternal(StringBuilder sb) {
        //return MARK + priority.toStringBrief() + SEPARATOR + durability.toStringBrief() + SEPARATOR + quality.toStringBrief() + MARK;

        CharSequence priorityString = Texts.n2(getPriority());
        CharSequence durabilityString = Texts.n2(getDurability());
        CharSequence qualityString = Texts.n2(getQuality());

        return toStringBuilder(sb, priorityString, durabilityString, qualityString);
    }

    default String toBudgetString() {
        return toBudgetStringExternal().toString();
    }

//    /**
//     * 1 digit resolution
//     */
//    default String toStringExternalBudget1(boolean includeQuality) {
//        char priorityString = Texts.n1char(getPriority());
//        char durabilityString = Texts.n1char(getDurability());
//        StringBuilder sb = new StringBuilder(1 + 1 + 1 + (includeQuality ? 1 : 0) + 1)
//                .append(Symbols.BUDGET_VALUE_MARK)
//                .append(priorityString).append(Symbols.VALUE_SEPARATOR)
//                .append(durabilityString);
//
//        if (includeQuality)
//            sb.append(Symbols.VALUE_SEPARATOR).append(Texts.n1char(getQuality()));
//
//        return sb.append(Symbols.BUDGET_VALUE_MARK).toString();
//    }

    default String getBudgetString() {
        return Budget.toString(this);
    }

    static String toString(Budget b) {
        //return MARK + Texts.n4(b.getPriority()) + SEPARATOR + Texts.n4(b.getDurability()) + SEPARATOR + Texts.n4(b.getQuality()) + MARK;
        return Budget.toStringBuilder(new StringBuilder(), Texts.n4(b.getPriority()), Texts.n4(b.getDurability()), Texts.n4(b.getQuality())).toString();
    }

    static StringBuilder toStringBuilder(StringBuilder sb, CharSequence priorityString, CharSequence durabilityString, CharSequence qualityString) {
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

        return sb;
    }



    default void set(Budget b) {
        budget(b.getPriority(), b.getDurability(), b.getQuality());
    }

}
