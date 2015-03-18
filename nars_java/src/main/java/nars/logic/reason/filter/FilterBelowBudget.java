package nars.logic.reason.filter;

import nars.logic.NAL;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;


public class FilterBelowBudget implements NAL.DerivationFilter {

    public final static String INSUFFICIENT_BUDGET = "Insufficient Budget";

    @Override public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        if (!task.budget.aboveThreshold()) return INSUFFICIENT_BUDGET;
        return null;
    }
}
