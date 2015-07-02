package nars.task.filter;

import com.google.common.util.concurrent.AtomicDouble;
import nars.nal.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;


/** this should not be used in practice because derivations should be allowed to accumulate in the derivation buffer,
 * after which the result will be filtered. otherwise potential solutions may be
 * discarded if their individual budgets are considered seperately whereas
 * if they combine, they will be above budget. */
public class FilterBelowBudget implements DerivationFilter {

    public final static String INSUFFICIENT_BUDGET = "Insufficient Budget";

    final AtomicDouble threshold;

    public FilterBelowBudget(AtomicDouble threshold) {
        this.threshold = threshold;
    }

    @Override public String reject(NAL nal, TaskSeed task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        if (!task.summaryGreaterOrEqual(threshold)) return INSUFFICIENT_BUDGET;
        return null;
    }
}
