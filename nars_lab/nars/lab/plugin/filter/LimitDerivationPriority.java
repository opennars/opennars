/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.lab.plugin.filter;

import nars.control.DerivationContext;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Task;

/**
 * experimental: task priority conservation based on NAL's current task
 * @author me
 */
public class LimitDerivationPriority implements DerivationContext.DerivationFilter {

    @Override
    public String reject(DerivationContext nal, Task task, boolean revised, boolean single, Task parent, Sentence otherBelief) {

        BudgetValue currentTaskBudget = nal.getCurrentTask().getBudget();
        task.budget.andPriority(currentTaskBudget.getPriority());
        
        return null;
    }
    
}
