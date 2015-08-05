/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.task.filter;

import nars.budget.Budget;
import nars.premise.Premise;
import nars.task.TaskSeed;

/**
 * experimental: task priority conservation based on NAL's current task
 * @author me
 */
public class LimitDerivationPriority implements DerivationFilter {

    @Override
    public String reject(Premise nal, TaskSeed task, boolean solution, boolean revised) {

        Budget currentTaskBudget = nal.getTask().getBudget();
        task.andPriority(currentTaskBudget.getPriority());
        
        return null;
    }
    
}
