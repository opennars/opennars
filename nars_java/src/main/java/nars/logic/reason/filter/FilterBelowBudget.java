package nars.logic.reason.filter;

import nars.logic.NAL;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;


public class FilterBelowBudget implements NAL.DerivationFilter {
    @Override public String reject(NAL nal, Task task, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        if (!task.budget.aboveThreshold()) return "Insufficient Budget";
        return null;
    }
}
