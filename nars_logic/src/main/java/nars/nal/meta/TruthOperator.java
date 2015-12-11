package nars.nal.meta;

import nars.Memory;
import nars.Premise;
import nars.nal.RuleMatch;
import nars.truth.Truth;



public interface TruthOperator {
    Truth apply(Truth task, Truth belief, Memory m);
    boolean allowOverlap();

    default boolean apply(RuleMatch m) {
        Premise premise = m.premise;
        Truth truth = apply(
                premise.getTask().getTruth(),
                premise.getBelief() == null ? null : premise.getBelief().getTruth(),
                premise.memory()
        );
        if (truth!=null) {
            m.truth.set(truth);
            return true;
        }
        return false;
    }
}
