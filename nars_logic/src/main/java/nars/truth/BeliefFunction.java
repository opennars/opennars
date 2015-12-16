package nars.truth;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.nal.meta.TruthOperator;
import nars.term.Term;
import nars.term.atom.Atom;

import java.util.Map;

/**
 * http://aleph.sagemath.org/?q=qwssnn
 <patham9> only strong rules are allowing overlap
 <patham9> except union and revision
 <patham9> if you look at the graph you see why
 <patham9> its both rules which allow the conclusion to be stronger than the premises
 */
public enum BeliefFunction implements TruthOperator {

    Revision() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            //if (B == null) return null;
            return TruthFunctions.revision(T, B);
        }
    },
    StructuralIntersection() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.intersection(B, newDefaultTruth(m));
        }
    },
    StructuralDeduction() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            //if (B == null) return null;
            return TruthFunctions.deduction1(T, defaultConfidence(m));
        }
    },
    StructuralAbduction() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.abduction(B, newDefaultTruth(m));
        }
    },
    Deduction(true) {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.deduction(T, B);
        }
    },
    Induction() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.induction(T, B);
        }
    },
    Abduction() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.abduction(T, B);
        }
    },
    Comparison() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.comparison(T, B);
        }
    },
    Conversion() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.conversion(B);
        }
    },
    Negation() {
        @Override public Truth apply(final Truth T, /* nullable */ final Truth B, Memory m) {
            return TruthFunctions.negation(T);
        }
    },
    Contraposition() {
        @Override public Truth apply(final Truth T, /* nullable */ final Truth B, Memory m) {
            return TruthFunctions.contraposition(T);
        }
    },
    Resemblance(true) {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.resemblance(T,B);
        }
    },
    Union() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.union(T,B);
        }
    },
    Intersection(true) {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.intersection(T,B);
        }
    },
    Difference(true) {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.difference(T,B);
        }
    },
    Analogy(true) {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.analogy(T,B);
        }
    },
    ReduceConjunction() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.reduceConjunction(T,B);
        }
    },
    ReduceDisjunction() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.reduceDisjunction(T, B);
        }
    },
    ReduceConjunctionNeg() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.reduceConjunctionNeg(T, B);
        }
    },
    AnonymousAnalogy() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B==null) return null;
            return TruthFunctions.anonymousAnalogy(T,B);
        }
    },
    Exemplification() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B==null) return null;
            return TruthFunctions.exemplification(T,B);
        }
    },
    DecomposeNegativeNegativeNegative() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B==null) return null;
            return TruthFunctions.decomposeNegativeNegativeNegative(T,B);
        }
    },
    DecomposePositiveNegativePositive() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B==null) return null;
            return TruthFunctions.decomposePositiveNegativePositive(T,B);
        }
    },
    DecomposeNegativePositivePositive() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B==null) return null;
            return TruthFunctions.decomposeNegativePositivePositive(T,B);
        }
    },
    DecomposePositivePositivePositive() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B==null) return null;
            return TruthFunctions.decomposeNegativePositivePositive(TruthFunctions.negation(T), B);
        }
    },
    DecomposePositiveNegativeNegative() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.decomposePositiveNegativeNegative(T,B);
        }
    },
    Identity() {
        @Override public Truth apply(final Truth T, /* nullable*/ final Truth B, Memory m) {
            return new DefaultTruth(T.getFrequency(), T.getConfidence());
        }
    },
    BeliefIdentity() {
        @Override public Truth apply(final Truth T, /* nullable*/ final Truth B, Memory m) {
            if (B == null) return null;
            return new DefaultTruth(B.getFrequency(), B.getConfidence());
        }
    },
    BeliefStructuralDeduction() {
        @Override public Truth apply(final Truth T, /* nullable*/ final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.deduction1(B, defaultConfidence(m));
        }
    },
    BeliefStructuralDifference() {
        @Override public Truth apply(final Truth T, /* nullable*/ final Truth B, Memory m) {
            if (B == null) return null;
            Truth res =  TruthFunctions.deduction1(B, defaultConfidence(m));
            return new DefaultTruth(1.0f-res.getFrequency(), res.getConfidence());
        }
    },
    BeliefNegation() {
        @Override public Truth apply(final Truth T, /* nullable*/ final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.negation(B);
        }
    };

    public static Truth newDefaultTruth(Memory m) {
        return m.newDefaultTruth(Symbols.JUDGMENT);
    }

    public static float defaultConfidence(Memory m) {
        return m.getDefaultConfidence(Symbols.JUDGMENT);
    }


    public final boolean allowOverlap;


//    /**
//     * @param T taskTruth
//     * @param B beliefTruth (possibly null)
//     * @return
//     */
//    @Override
//    abstract public Truth apply(Truth T, Truth B, Memory m);



    static final Map<Term, BeliefFunction> atomToTruthModifier = Global.newHashMap(BeliefFunction.values().length);

    static {
        for (BeliefFunction tm : BeliefFunction.values())
            atomToTruthModifier.put(Atom.the(tm.toString()), tm);
    }

    public static BeliefFunction get(Term a) {
        return atomToTruthModifier.get(a);
    }

    BeliefFunction() {
        this(false);
    }

    BeliefFunction(boolean allowOverlap) {
        this.allowOverlap = allowOverlap;
    }


    @Override
    public final boolean allowOverlap() {
        return allowOverlap;
    }
}
