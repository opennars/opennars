package nars.budget;

import static nars.budget.BudgetFunctions.m;

/**
 * Essentially the methods involved with the priority component of a Budget
 */
public interface Prioritized {

    float getPriority();

    boolean setPriority(float p);

    boolean addPriority(float v);

    boolean merge(Prioritized that);

    long setLastForgetTime(long currentTime);

    long getLastForgetTime();

    void setUsed(long now);

    boolean mulPriority(float factor);

    default public boolean maxPriority(final float otherPriority) {
        return setPriority(m(getPriority(), otherPriority)); //max durab
    }
}
