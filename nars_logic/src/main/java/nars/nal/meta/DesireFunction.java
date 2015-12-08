package nars.nal.meta;

import nars.Global;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.function.BinaryOperator;

public interface DesireFunction extends BinaryOperator<Truth>{
    DesireFunction
            Negation = (t1, ignored) -> TruthFunctions.negation(t1),
            Strong = (T, B) -> B == null ? null : TruthFunctions.desireStrong(T, B),
            Weak = (T, B) -> B == null ? null : TruthFunctions.desireWeak(T, B),
            Induction = (T, B) -> B == null ? null : TruthFunctions.desireInd(T, B),
            Deduction = (T, B) -> B == null ? null : TruthFunctions.desireDed(T, B),
            Identity = (T, B) -> new DefaultTruth(T.getFrequency(), T.getConfidence()),
            StructuralStrong = (T, ignored) -> TruthFunctions.desireStrong(T, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE))
    ;

}