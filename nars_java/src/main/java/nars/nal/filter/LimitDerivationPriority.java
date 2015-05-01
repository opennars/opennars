/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.filter;

import nars.nal.*;
import nars.budget.Budget;

/**
 * experimental: task priority conservation based on NAL's current task
 * @author me
 */
public class LimitDerivationPriority implements DerivationFilter {

    @Override
    public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {

        Budget currentTaskBudget = nal.getCurrentTask().getBudget();
        task.andPriority(currentTaskBudget.getPriority());
        
        return null;
    }
    
}
