package nars.budget;

import nars.util.data.Util;

import javax.annotation.Nullable;

import static nars.Global.BUDGET_EPSILON;
import static nars.nal.UtilityFunctions.aveGeo;
import static nars.nal.UtilityFunctions.aveGeoNotLessThan;
import static nars.util.data.Util.mean;

/**
 * Created by me on 12/11/15.
 */
public interface Budget extends Prioritized, Budgeted {
    Budget zero();

    @Override
    UnitBudget getBudget();

    @Override
    float getPriority();

    @Override
    void setPriority(float p);

    @Override
    long setLastForgetTime(long currentTime);

    @Override
    long getLastForgetTime();

    @Override
    void mulPriority(float factor);

    float getDurability();

    void setDurability(float d);

    float getQuality();

    void setQuality(float q);

    /** the max priority, durability, and quality of two tasks */
    default Budget mergeMax(Budget b) {
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
    default Budget mergePlus(Budget b) {
        return mergePlus(b, 1.0f);
    }

    default Budget mergePlus(Budget b, float factor) {
        return mergePlus(b.getPriority(), b.getDurability(), b.getQuality(), factor);
    }

    default Budget mergePlus(float addPriority, float otherDurability, float otherQuality) {
        return mergePlus(addPriority, otherDurability, otherQuality, 1.0f);
    }

    /** linearly interpolates the change affected to determine dur, qua */
    default Budget mergePlus(float addPriority, float otherDurability, float otherQuality, float factor) {

        float dp = addPriority * factor;

        float currentPriority = getPriorityIfNaNThenZero();

        float nextPriority = Math.min(1,currentPriority + dp);

        float currentNextPrioritySum = (currentPriority + nextPriority);

        /* current proportion */ float cp = (Util.equal(currentNextPrioritySum, 0, BUDGET_EPSILON)) ?
                0.5f : /* both are zero so they have equal infleunce */
                (currentPriority / currentNextPrioritySum);
        /* next proportion */ float np = 1.0f - cp;


        float D = getDurability();
        float Q = getQuality();
        return budget(
                nextPriority,
                Math.max(D, (cp * D) + (np * otherDurability)),
                Math.max(Q, (cp * Q) + (np * otherQuality))
        );
    }

    /**
     * merges another budget into this one, averaging each component
     */
    default void mergeAverageLERP(Budget that) {
        if (this == that) return;

        float currentPriority = getPriority();

        float otherPriority = that.getPriority();

        float prisum = (currentPriority + otherPriority);

        /* current proportion */
        float cp = (Util.equal(prisum, 0, BUDGET_EPSILON)) ?
                0.5f : /* both are zero so they have equal infleunce */
                (currentPriority / prisum);

        /* next proportion */
        float np = 1.0f - cp;

        budget(
                cp * getPriority() + np * that.getPriority(),
                cp * getDurability() + np * that.getDurability(),
                cp * getQuality() + np * that.getQuality()
        );
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


    default boolean isZero() {
        return summaryLessThan(BUDGET_EPSILON);
    }

    default boolean summaryLessThan(float s) {
        return !summaryNotLessThan(s);
    }

    /**
     * uses optimized aveGeoNotLessThan to avoid a cube root operation
     */
    default boolean summaryNotLessThan(float min) {
        return aveGeoNotLessThan(min, getPriority(), getDurability(), getQuality());
    }


    /** copies a budget into this; if source is null, it deletes the budget */
    default Budget budget(@Nullable Budget source) {
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
    default Budget budget(float p, float d, float q) {
        setPriority(p);
        setDurability(d);
        setQuality(q);
        return this;
    }

    default Budget budget(Budgeted source) {
        return budget(source.getBudget());
    }


    default float getPriorityIfNaNThenZero() {
        float p;
        if (!Float.isNaN(p = getPriority()))
            return p;
        return 0;
    }

}
