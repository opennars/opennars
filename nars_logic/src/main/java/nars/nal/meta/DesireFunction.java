package nars.nal.meta;

import nars.Memory;
import nars.Symbols;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

public interface DesireFunction extends TruthOperator {
    DesireFunction
            Negation = (t1, ignored, m) -> TruthFunctions.negation(t1),
            Strong = (T, B, m) -> B == null ? null : TruthFunctions.desireStrong(T, B),
            Weak = (T, B, m) -> B == null ? null : TruthFunctions.desireWeak(T, B),
            Induction = (T, B, m) -> B == null ? null : TruthFunctions.desireInd(T, B),
            Deduction = (T, B, m) -> B == null ? null : TruthFunctions.desireDed(T, B),
            Identity = (T, B, m) -> new DefaultTruth(T.getFrequency(), T.getConfidence()),
            StructuralStrong = (T, ignored, m) -> TruthFunctions.desireStrong(T, GOAL(m))
    ;

    static Truth GOAL(Memory m) {
        return new DefaultTruth(1.0f, m.getDefaultConfidence(Symbols.GOAL));
    }

}