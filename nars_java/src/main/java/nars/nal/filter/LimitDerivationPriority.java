/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.filter;

import nars.nal.Item;
import nars.nal.NAL;
import nars.budget.Budget;
import nars.nal.Sentence;
import nars.nal.Task;

/**
 * experimental: task priority conservation based on NAL's current task
 * @author me
 */
public class LimitDerivationPriority implements NAL.DerivationFilter {

    @Override
    public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {

        Budget currentTaskBudget = nal.getCurrentTask().getBudget();
        task.andPriority(currentTaskBudget.getPriority());
        
        return null;
    }
    
}
