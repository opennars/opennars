package nars.meta;

import nars.Global;
import nars.term.Atom;
import nars.term.Term;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.Map;

/**
 * Created by me on 8/1/15.
 */
public enum TruthFunction {

    Revision() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.revision(T, B);
        }
    },
    StructuralIntersection() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.intersection(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
        }
    },
    StructuralDeduction() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.deduction(T, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
        }
    },
    StructuralAbduction() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.abduction(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
        }
    },
    Deduction(true) {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.deduction(T, B);
        }
    },
    Induction() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.induction(T, B);
        }
    },
    Abduction() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.abduction(T, B);
        }
    },
    Comparison() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.comparison(T, B);
        }
    },
    Conversion() {
        @Override public Truth get(final Truth T, /*nullable*/ final Truth B) {
            if (B == null) return null;
            return TruthFunctions.conversion(B);
        }
    },
    Negation() {
        @Override public Truth get(final Truth T, final Truth B) { return TruthFunctions.negation(T); }
    },
    Contraposition() {
        @Override public Truth get(final Truth T, /* nullable */ final Truth B) {
            return TruthFunctions.contraposition(T);
        }
    },
    Resemblance() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.resemblance(T,B);
        }
    },
    Union() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.union(T,B);
        }
    },
    Intersection() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.intersection(T,B);
        }
    },
    Difference() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.difference(T,B);
        }
    },
    Analogy(true) {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.analogy(T,B);
        }
    },
    ReduceConjunction() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.reduceConjunction(T,B);
        }
    },
    ReduceDisjunction() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.reduceDisjunction(T, B);
        }
    },
    ReduceConjunctionNeg() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.reduceConjunctionNeg(T, B);
        }
    },
    AnonymousAnalogy() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B==null) return null;
            return TruthFunctions.anonymousAnalogy(T,B);
        }
    },
    Exemplification() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B==null) return null;
            return TruthFunctions.exemplification(T,B);
        }
    },
    DecomposeNegativeNegativeNegative() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B==null) return null;
            return TruthFunctions.decomposeNegativeNegativeNegative(T,B);
        }
    },
    DecomposePositiveNegativePositive() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B==null) return null;
            return TruthFunctions.decomposePositiveNegativePositive(T,B);
        }
    },
    DecomposeNegativePositivePositive() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B==null) return null;
            return TruthFunctions.decomposeNegativePositivePositive(T,B);
        }
    },
    DecomposePositiveNegativeNegative() {
        @Override public Truth get(final Truth T, final Truth B) {
            if (B == null) return null;
            return TruthFunctions.decomposePositiveNegativeNegative(T,B);
        }
    },
    Identity() {
        @Override public Truth get(final Truth T, /* nullable*/ final Truth B) {
            return new DefaultTruth(T.getFrequency(), T.getConfidence());
        }
    },
    BeliefIdentity() {
        @Override public Truth get(final Truth T, /* nullable*/ final Truth B) {
            return new DefaultTruth(B.getFrequency(), B.getConfidence());
        }
    },
    BeliefStructuralDeduction() {
        @Override public Truth get(final Truth T, /* nullable*/ final Truth B) {
            return TruthFunctions.deduction(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
        }
    },
    BeliefStructuralDifference() {
        @Override public Truth get(final Truth T, /* nullable*/ final Truth B) {
            Truth res =  TruthFunctions.deduction(B, new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE));
            return new DefaultTruth(1.0f-res.getFrequency(), res.getConfidence());
        }
    },
    BeliefNegation() {
        @Override public Truth get(final Truth T, /* nullable*/ final Truth B) {
            return TruthFunctions.negation(B);
        }
    }
    ;



    public final boolean allowOverlap;


    /**
     * @param T taskTruth
     * @param B beliefTruth (possibly null)
     * @return
     */
    abstract public Truth get(Truth T, Truth B);



    static final Map<Term, TruthFunction> atomToTruthModifier = Global.newHashMap(TruthFunction.values().length);

    static {
        for (TruthFunction tm : TruthFunction.values())
            atomToTruthModifier.put(Atom.the(tm.toString()), tm);
    }

    public static TruthFunction get(Term a) {
        return atomToTruthModifier.get(a);
    }

    TruthFunction() {
        this(false);
    }

    TruthFunction(boolean allowOverlap) {
        this.allowOverlap = allowOverlap;
    }
}
