package nars.task.filter;

import com.google.common.util.concurrent.AtomicDouble;
import nars.premise.Premise;
import nars.task.TaskSeed;


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
    public String reject(final Premise nal, final TaskSeed task, final boolean solution, final boolean revised) {
        if (!solution) {
            final TaskSeed derived = task;
            if (!leak(derived))
                return "MultiplyDerivedBudget";
        }
        return null;
    }

    protected boolean leak(TaskSeed derived) {
        derived.mulPriority(priorityMultiplier.floatValue());
        derived.mulDurability(durabilityMultiplier.floatValue());
        return true;
    }


}
