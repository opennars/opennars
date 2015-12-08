package nars.nal.meta;

import nars.Memory;
import nars.Symbols;
import nars.java.AtomObject;
import nars.java.DefaultTermizer;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

import static nars.$.ref;
import static nars.truth.TruthFunctions.*;

public interface BeliefFunction extends TruthOperator {

    static Truth JUDGMENT(Memory m) {
        return new DefaultTruth(1f, m.getDefaultConfidence(Symbols.JUDGMENT));
    }

    BeliefFunction
            Revision = (T, B, m) -> revision(T, B),
            StructuralIntersection = (T, B, m) -> B == null ? null : intersection(B, JUDGMENT(m))    ,
            StructuralAbduction = (T, B, m) -> B == null ? null : abduction(B, JUDGMENT(m)),
            Deduction = (T, B, m) -> B == null ? null : deduction(T, B),
            Induction = (T, B, m) -> B == null ? null : induction(T, B),
            Abduction = (T, B, m) -> B == null ? null : abduction(T, B),
            Comparison = (T, B, m) -> B == null ? null : comparison(T, B),
            Conversion = (T, B, m) -> B == null ? null : conversion(B),
            Negation = (T, B, m) -> negation(T),
            Contraposition = (T, B, m) -> contraposition(T),
            Union = (T, B, m) -> B == null ? null : union(T, B),
            ReduceConjunction = (T, B, m) -> B == null ? null : reduceConjunction(T, B),
            ReduceDisjunction = (T, B, m) -> B == null ? null : reduceDisjunction(T, B),
            ReduceConjunctionNeg = (T, B, m) -> B == null ? null : reduceConjunctionNeg(T, B),
            AnonymousAnalogy = (T, B, m) -> B == null ? null : anonymousAnalogy(T, B),
            Exemplification = (T, B, m) -> B == null ? null : exemplification(T, B),
            DecomposeNegativeNegativeNegative = (T, B, m) -> B == null ? null : decomposeNegativeNegativeNegative(T, B),
            DecomposePositiveNegativePositive = (T, B, m) -> B == null ? null : decomposePositiveNegativePositive(T, B),
            DecomposeNegativePositivePositive = (T, B, m) -> B == null ? null : decomposeNegativePositivePositive(T, B),
            DecomposePositivePositivePositive = (T, B, m) -> B == null ? null : decomposeNegativePositivePositive(negation(T), B),
            DecomposePositiveNegativeNegative = (T, B, m) -> B == null ? null : decomposePositiveNegativeNegative(T, B),
            Identity = (T, B, m) -> new DefaultTruth(T.getFrequency(), T.getConfidence()),
            BeliefIdentity = (T, B, m) -> B == null ? null : new DefaultTruth(B.getFrequency(), B.getConfidence()),
            BeliefStructuralDeduction = (T, B, m) -> B == null ? null : deduction(B, JUDGMENT(m)),

    BeliefStructuralDifference = (T, B, m) -> {
                if (B == null) return null;
                else {
                    Truth res = deduction(B, JUDGMENT(m));
                    return new DefaultTruth(1.0f - res.getFrequency(), res.getConfidence());
                }
            },
            BeliefNegation = (T, B, m) -> B == null ? null : negation(B);

    BeliefFunctionCyclicAllowed
            StructuralDeduction = (T, B, m) -> deduction(T, JUDGMENT(m)),
            Resemblance = (T, B, m) -> B == null ? null : resemblance(T, B),
            Intersection = (T, B, m) -> B == null ? null : intersection(T, B),
            Difference= (T, B, m) -> B == null ? null : difference(T, B),
            Analogy = (T, B, m) -> B == null ? null : analogy(T, B);



    interface BeliefFunctionCyclicAllowed extends CanCycle, BeliefFunction {
    }


    class the {

        private static final Map<Atom, AtomObject<TruthOperator>>
                beliefFuncs;

        private static final Map<Atom, AtomObject<TruthOperator>>
                desireFuncs;

        static {


            Function<Field, AtomObject<TruthOperator>> fieldAtomFunction = f-> {
                try {
                    return
                        ref(f.getName(),
                            ((TruthOperator) f.get(null)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            };

            beliefFuncs = DefaultTermizer.mapStaticClassFields(
                    BeliefFunction.class,
                    fieldAtomFunction);

            desireFuncs = DefaultTermizer.mapStaticClassFields(
                    DesireFunction.class,
                    fieldAtomFunction);

        }

        public static TruthOperator belief(Term which) {
            return beliefFuncs.get(which).get();
        }

        public static TruthOperator desire(Term which) {
            AtomObject<TruthOperator> f = desireFuncs.get(which);
            if (f == null) return null;
            return f.get();
        }
    }
}
