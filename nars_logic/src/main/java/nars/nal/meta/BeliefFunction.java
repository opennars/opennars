package nars.nal.meta;

import nars.$;
import nars.Global;
import nars.term.Term;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static nars.truth.TruthFunctions.*;

public interface BeliefFunction extends BinaryOperator<Truth> {
    BeliefFunction
            Revision = (T, B) -> revision(T, B),
            StructuralIntersection = (T, B) -> B == null ? null : intersection(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)),
            StructuralAbduction = (T, B) -> B == null ? null : abduction(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)),
            Deduction = (T, B) -> B == null ? null : deduction(T, B),
            Induction = (T, B) -> B == null ? null : induction(T, B),
            Abduction = (T, B) -> B == null ? null : abduction(T, B),
            Comparison = (T, B) -> B == null ? null : comparison(T, B),
            Conversion = (T, B) -> B == null ? null : conversion(B),
            Negation = (T, B) -> negation(T),
            Contraposition = (T, B) -> contraposition(T),
            Union = (T, B) -> B == null ? null : union(T, B),
            ReduceConjunction = (T, B) -> B == null ? null : reduceConjunction(T, B),
            ReduceDisjunction = (T, B) -> B == null ? null : reduceDisjunction(T, B),
            ReduceConjunctionNeg = (T, B) -> B == null ? null : reduceConjunctionNeg(T, B),
            AnonymousAnalogy = (T, B) -> B == null ? null : anonymousAnalogy(T, B),
            Exemplification = (T, B) -> B == null ? null : exemplification(T, B),
            DecomposeNegativeNegativeNegative = (T, B) -> B == null ? null : decomposeNegativeNegativeNegative(T, B),
            DecomposePositiveNegativePositive = (T, B) -> B == null ? null : decomposePositiveNegativePositive(T, B),
            DecomposeNegativePositivePositive = (T, B) -> B == null ? null : decomposeNegativePositivePositive(T, B),
            DecomposePositivePositivePositive = (T, B) -> B == null ? null : decomposeNegativePositivePositive(negation(T), B),
            DecomposePositiveNegativeNegative = (T, B) -> B == null ? null : decomposePositiveNegativeNegative(T, B),
            Identity = (T, B) -> new DefaultTruth(T.getFrequency(), T.getConfidence()),
            BeliefIdentity = (T, B) -> B == null ? null : new DefaultTruth(B.getFrequency(), B.getConfidence()),
            BeliefStructuralDeduction = (T, B) -> B == null ? null : deduction(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)),
            BeliefStructuralDifference = (T, B) -> {
                if (B == null) return null;else {
                Truth res = deduction(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
                return new DefaultTruth(1.0f - res.getFrequency(), res.getConfidence());}
            },
            BeliefNegation = (T, B) -> B == null ? null : negation(B);
    OverlappedBelief
            StructuralDeduction = (T, B) -> deduction(T, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)),
            Resemblance = (T, B) -> B == null ? null : resemblance(T, B),
            Intersection = (T, B) -> B == null ? null : intersection(T, B),
            Difference = (T, B) -> B == null ? null : difference(T, B),
            Analogy = (T, B) -> B == null ? null : analogy(T, B);

    Map<Term, BinaryOperator<Truth>> atomToTruthModifier = Arrays.stream(new BinaryOperator[]{
            Revision,
            StructuralIntersection,
            StructuralDeduction,
            StructuralAbduction,
            Deduction,
            Induction,
            Abduction,
            Comparison,
            Conversion,
            Negation,
            Contraposition,
            Resemblance,
            Union,
            Intersection,
            Difference,
            Analogy,
            ReduceConjunction,
            ReduceDisjunction,
            ReduceConjunctionNeg,
            AnonymousAnalogy,
            Exemplification,
            DecomposeNegativeNegativeNegative,
            DecomposePositiveNegativePositive,
            DecomposeNegativePositivePositive,
            DecomposePositivePositivePositive,
            DecomposePositiveNegativeNegative,
            Identity,
            BeliefIdentity,
            BeliefStructuralDeduction,
            BeliefStructuralDifference,
            BeliefNegation
    }).collect(Collectors.toMap(p -> $.the(p.getClass().getCanonicalName()), p -> p));

    interface OverlappedBelief extends Overlapped, BeliefFunction {
    }

    class Helper {
        public static BinaryOperator<Truth> apply(Term which) {
            return atomToTruthModifier.get(which);
        }
    }
}
