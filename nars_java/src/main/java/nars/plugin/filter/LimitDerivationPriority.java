/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.plugin.filter;

import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Task;

/**
 * experimental: task priority conservation based on NAL's current task
 * @author me
 */
public class LimitDerivationPriority implements NAL.DerivationFilter {

    @Override
    public String reject(NAL nal, Task task, boolean revised, boolean single, Task parent, Sentence otherBelief) {

        BudgetValue currentTaskBudget = nal.getCurrentTask().getBudget();
        task.budget.andPriority(currentTaskBudget.getPriority());
        
        return null;
    }
    
}
