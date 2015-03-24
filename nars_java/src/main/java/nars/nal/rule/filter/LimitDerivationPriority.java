/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.rule.filter;

import nars.nal.NAL;
import nars.energy.Budget;
import nars.nal.entity.Sentence;
import nars.nal.entity.Task;

/**
 * experimental: task priority conservation based on NAL's current task
 * @author me
 */
public class LimitDerivationPriority implements NAL.DerivationFilter {

    @Override
    public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {

        Budget currentTaskBudget = nal.getCurrentTask().getBudget();
        task.budget.andPriority(currentTaskBudget.getPriority());
        
        return null;
    }
    
}
