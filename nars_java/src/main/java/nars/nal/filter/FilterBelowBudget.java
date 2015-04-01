package nars.nal.filter;

import nars.nal.NAL;
import nars.nal.Sentence;
import nars.nal.Task;


public class FilterBelowBudget implements NAL.DerivationFilter {

    public final static String INSUFFICIENT_BUDGET = "Insufficient Budget";

    @Override public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        if (!task.budget.aboveThreshold()) return INSUFFICIENT_BUDGET;
        return null;
    }
}
