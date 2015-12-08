package nars.nal.meta;

import nars.Global;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

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

    Map<Term, BinaryOperator<Truth>> atomToTruthModifier = Arrays.stream(
            new BinaryOperator[]{Negation, Strong, Weak, Induction, Deduction, Identity, StructuralStrong,}
    ).collect(Collectors.toMap( Atom::the, (p) -> p));

    class Helper {static  BinaryOperator<Truth> apply(Term a) {
        return atomToTruthModifier.get(a);
    }
    }
}