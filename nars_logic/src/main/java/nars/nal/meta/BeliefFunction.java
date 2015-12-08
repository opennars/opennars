package nars.nal.meta;

import nars.$;
import nars.Global;
import nars.java.AtomObject;
import nars.java.DefaultTermizer;
import nars.nal.nal3.SetExt;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static nars.$.ref;
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
                if (B == null) return null;
                else {
                    Truth res = deduction(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
                    return new DefaultTruth(1.0f - res.getFrequency(), res.getConfidence());
                }
            },
            BeliefNegation = (T, B) -> B == null ? null : negation(B);
    CanCycleBelief
            StructuralDeduction = (T, B) -> deduction(T, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)),
            Resemblance = (T, B) -> B == null ? null : resemblance(T, B),
            Intersection = (T, B) -> B == null ? null : intersection(T, B),
            Difference = (T, B) -> B == null ? null : difference(T, B),
            Analogy = (T, B) -> B == null ? null : analogy(T, B);


    interface CanCycleBelief extends CanCycle, BeliefFunction {
    }


    ////TODO cleanup
    class Helper {

        static final Map<? super Atom, AtomObject<BinaryOperator<Truth>>>
                atomAtomObjectMap = Global.newHashMap(32);


        static {


            Function<Field, Atom> fieldAtomFunction = f-> {
                try {
                    Atom a = $.$(f.getName());
                    AtomObject<BinaryOperator<Truth>> exist = atomAtomObjectMap.put(
                            a,
                            ref(f.getName(),
                                    ((BinaryOperator<Truth>) f.get(null)))
                    );
                    /*if (exist!=null)
                        throw new RuntimeException("conflict");*/
                    return a;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            };

            SetExt<Atom> beliefFuncs = DefaultTermizer.getStaticClassFields(
                    BeliefFunction.class,
                    fieldAtomFunction);

            SetExt<Atom> desireFuncs = DefaultTermizer.getStaticClassFields(
                    DesireFunction.class,
                    fieldAtomFunction);

        }

        public static BinaryOperator<Truth> apply(Term which) {
            return atomAtomObjectMap.get(which).get();
        }
    }
}
