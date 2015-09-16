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

        limitDerivation(nal, task);
        
        return null;
    }

    public static void limitDerivation(Premise nal, Task task) {
        final Budget targetBudget = task.getBudget();

        final Budget currentTaskBudget = nal.getTask().getBudget();
        float m = currentTaskBudget.getPriority();

        if (nal.getBelief()!=null) {
            final Budget currentBeliefBudget = nal.getBelief().getBudget();
            m = UtilityFunctions.and(m,currentBeliefBudget.getPriority());
        }

        targetBudget.andPriority(m);
    }
    /** limits budget to be no more than its parents */
    public static Task limitDerivation(final Task task) {

        float total = 0;
        int n = 0;

        Task pt = task.getParentTask();
        if (pt!=null) {
            total += pt.getBudget().summary();
            n++;
        }
        Task pb = task.getParentBelief();
        if (pb!=null) {
            total += pb.getBudget().summary();
            n++;
        }

        /** input/original source */
        if (n == 0) return task;

        float avg = total/n;
        task.getBudget().andPriority(avg);

        return task;
    }


}
