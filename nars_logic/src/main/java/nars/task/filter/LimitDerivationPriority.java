/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.task.filter;

import nars.budget.Budget;
import nars.nal.UtilityFunctions;
import nars.premise.Premise;
import nars.task.Task;

/**
 * experimental: task priority conservation based on NAL's current task
 * @author me
 */
public class LimitDerivationPriority implements DerivationFilter {

    @Override
    final public String reject(final Premise nal, final Task task, final boolean solution, final boolean revised) {

        final Budget targetBudget = task.getBudget();

        final Budget currentTaskBudget = nal.getTask().getBudget();
        float m = currentTaskBudget.getPriority();

        if (nal.getBelief()!=null) {
            final Budget currentBeliefBudget = nal.getBelief().getBudget();
            m = UtilityFunctions.and(m,currentBeliefBudget.getPriority());
        }

        targetBudget.andPriority(m);
        
        return null;
    }
    
}
