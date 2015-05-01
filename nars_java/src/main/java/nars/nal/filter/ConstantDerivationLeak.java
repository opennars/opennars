package nars.nal.filter;

import nars.nal.DerivationFilter;
import nars.nal.NAL;
import nars.nal.Sentence;
import nars.nal.Task;
import reactor.jarjar.jsr166e.extra.AtomicDouble;


/**
 * Created by me on 5/1/15.
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
    public String reject(final NAL nal, final Task task, final boolean solution, final boolean revised, final boolean single, final Sentence currentBelief, final Task currentTask) {
        if (!solution) {
            final Task derived = task;
            derived.mulPriority(priorityMultiplier.floatValue());
            derived.mulDurability(durabilityMultiplier.floatValue());
        }
        return null;
    }


}
