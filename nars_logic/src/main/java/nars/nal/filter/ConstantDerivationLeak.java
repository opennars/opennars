package nars.nal.filter;

import com.google.common.util.concurrent.AtomicDouble;
import nars.nal.DerivationFilter;
import nars.nal.NAL;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.task.TaskSeed;


/**
 * TODO make abstract and put the AtomicDouble params in a DefaultLeak class
 */
public class ConstantDerivationLeak implements DerivationFilter {

    public final AtomicDouble priorityMultiplier;
    public final AtomicDouble durabilityMultiplier;

    public ConstantDerivationLeak(float priorityMultiplier, float durabilityMultiplier) {
        this(new AtomicDouble(priorityMultiplier), new AtomicDouble(durabilityMultiplier));
    }

    public ConstantDerivationLeak(AtomicDouble priorityMultiplier, AtomicDouble durabilityMultiplier) {
        this.priorityMultiplier = priorityMultiplier;
        this.durabilityMultiplier = durabilityMultiplier;
    }


    @Override
    public String reject(final NAL nal, final TaskSeed task, final boolean solution, final boolean revised, final boolean single, final Sentence currentBelief, final Task currentTask) {
        if (!solution) {
            final TaskSeed derived = task;
            if (!leak(derived))
                return "Leak";
        }
        return null;
    }

    protected boolean leak(TaskSeed derived) {
        derived.mulPriority(priorityMultiplier.floatValue());
        derived.mulDurability(durabilityMultiplier.floatValue());
        return true;
    }


}
