package nars.budget;

/**
 * Budget merge function, with input scale factor
 */
@FunctionalInterface
public interface BudgetMerge {

    /** merge 'incoming' budget (scaled by incomingScale) into 'existing' */
    void merge(Budget existing, Budget incoming, float incomingScale);
}
