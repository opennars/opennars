package org.opennars.control;

import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.inference.UtilityFunctions;

import static java.lang.Math.pow;

public class Attention {
    /**
     * Decrease Priority after an item is used, called in Bag.
     * After a constant time, p should become d*p. Since in this period, the
     * item is accessed c*p times, each time p-q should multiple d^(1/(c*p)).
     * The intuitive meaning of the parameter "forgetRate" is: after this number
     * of times of access, priority 1 will become d, it is a system parameter
     * adjustable in run time.
     *
     * @param budget The previous budget value
     * @param forgetCycles The budget for the new item
     * @param relativeThreshold The relative threshold of the bag
     */
    public static float applyForgetting(final BudgetValue budget, final float forgetCycles, final float relativeThreshold) {
        float quality = budget.getQuality() * relativeThreshold;      // re-scaled quality
        final float p = budget.getPriority() - quality;                     // priority above quality
        if (p > 0) {
            quality += p * pow(budget.getDurability(), 1.0 / (forgetCycles * p));
        }    // priority Durability
        budget.setPriority(quality);
        return quality;
    }

    public static BudgetValue solutionEvalTask(Sentence solution, Task task, BudgetValue budget, boolean judgmentTask, float quality) {
        if (judgmentTask) {
            task.incPriority(quality);
        } else {
            float taskPriority = task.getPriority(); // +goal satisfication is a matter of degree - https://groups.google.com/forum/#!topic/open-nars/ZfCM416Dx1M
            budget = new BudgetValue(UtilityFunctions.or(taskPriority, quality), task.getDurability(), BudgetFunctions.truthToQuality(solution.truth));
            task.setPriority(Math.min(1 - quality, taskPriority));
        }
        return budget;
    }

    public static void solutionEvalLinkFeedback(DerivationContext nal, float quality) {
        TaskLink tLink = nal.getCurrentTaskLink();
        tLink.setPriority(Math.min(1 - quality, tLink.getPriority()));
        TermLink bLink = nal.getCurrentBeliefLink();
        bLink.incPriority(quality);
    }




    public static void updatePriorityAnd(Task task, float priorityGain) {
        task.setPriority(UtilityFunctions.and(task.getPriority(), priorityGain));
    }

    public static void updatePriorityOr(Task task, float priorityGain) {
        task.setPriority(BudgetFunctions.or(task.getPriority(), priorityGain));
    }
}
