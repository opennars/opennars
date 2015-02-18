/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic.reason.filter;

import nars.logic.NAL;
import nars.logic.entity.BudgetValue;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;

/**
 * experimental: task priority conservation based on NAL's current task
 * @author me
 */
public class LimitDerivationPriority implements NAL.DerivationFilter {

    @Override
    public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {

        BudgetValue currentTaskBudget = nal.getCurrentTask().getBudget();
        task.budget.andPriority(currentTaskBudget.getPriority());
        
        return null;
    }
    
}
