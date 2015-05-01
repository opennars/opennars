package nars.nal.filter;

import nars.nal.*;


public class FilterBelowBudget implements DerivationFilter {

    public final static String INSUFFICIENT_BUDGET = "Insufficient Budget";

    @Override public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        if (!task.aboveThreshold()) return INSUFFICIENT_BUDGET;
        return null;
    }
}
