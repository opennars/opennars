package nars.task.filter;

import com.google.common.util.concurrent.AtomicDouble;
import nars.premise.Premise;
import nars.task.Task;


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

    @Override public final String reject(final Premise nal, final Task task, boolean solution, boolean revised) {
        if (!task.getBudget().summaryGreaterOrEqual(threshold)) return INSUFFICIENT_BUDGET;
        return VALID;
    }
}
