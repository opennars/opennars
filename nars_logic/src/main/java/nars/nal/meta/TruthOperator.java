package nars.nal.meta;

import nars.Memory;
import nars.Premise;
import nars.nal.PremiseMatch;
import nars.truth.DefaultTruth;
import nars.truth.Truth;



public interface TruthOperator {
    Truth apply(Truth task, Truth belief, Memory m);
    boolean allowOverlap();

    default boolean  apply(PremiseMatch m) {
        Premise premise = m.premise;
        Truth truth = apply(
                premise.getTask().getTruth(),
                premise.getBelief() == null ? null : premise.getBelief().getTruth(),
                premise.memory()
        );

        if (truth!=null) {
            //pre-filter insufficient confidence level
            if (truth.getConfidence() < DefaultTruth.DEFAULT_TRUTH_EPSILON) {
                return false;
            }

            m.truth.set(truth);
            return true;
        }
        return false;
    }
}
