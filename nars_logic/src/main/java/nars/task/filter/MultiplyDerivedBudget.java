package nars.task.filter;

import com.google.common.util.concurrent.AtomicDouble;
import nars.budget.Budget;
import nars.premise.Premise;
import nars.task.Task;


/**
 * TODO make abstract and put the AtomicDouble params in a DefaultLeak class
 */
public class MultiplyDerivedBudget implements DerivationFilter {

    public final AtomicDouble priorityMultiplier;
    public final AtomicDouble durabilityMultiplier;

    public MultiplyDerivedBudget(float priorityMultiplier, float durabilityMultiplier) {
        this(new AtomicDouble(priorityMultiplier), new AtomicDouble(durabilityMultiplier));
    }

    public MultiplyDerivedBudget(AtomicDouble priorityMultiplier, AtomicDouble durabilityMultiplier) {
        this.priorityMultiplier = priorityMultiplier;
        this.durabilityMultiplier = durabilityMultiplier;
    }


    @Override
    final public String reject(final Premise nal, final Task derived, final boolean solution, final boolean revised) {
        if (!solution) {
            if (!leak(derived))
                return "MultiplyDerivedBudget";
        }
        return null;
    }

    final protected boolean leak(final Task derived) {
        final Budget b = derived.getBudget();
        b.mulPriority(priorityMultiplier.floatValue());
        b.mulDurability(durabilityMultiplier.floatValue());
        return true;
    }


}
