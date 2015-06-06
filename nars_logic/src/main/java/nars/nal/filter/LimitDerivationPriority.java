/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.filter;

import nars.budget.Budget;
import nars.nal.DerivationFilter;
import nars.nal.NAL;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.task.TaskSeed;

/**
 * experimental: task priority conservation based on NAL's current task
 * @author me
 */
public class LimitDerivationPriority implements DerivationFilter {

    @Override
    public String reject(NAL nal, TaskSeed task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {

        Budget currentTaskBudget = nal.getCurrentTask().getBudget();
        task.andPriority(currentTaskBudget.getPriority());
        
        return null;
    }
    
}
