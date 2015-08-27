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
    Deduction() {
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
            return TruthFunctions.abduction(T, B);
        }
    },
    Comparison() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.comparison(T, B);
        }
    },
    Conversion() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.conversion(T);
        }
    },
    Negation() {
        @Override public Truth get(final Truth T, final Truth B) { return TruthFunctions.negation(T); }
    },
    Contraposition() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.contraposition(T);
        }
    },
    Resemblance() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.resemblance(T,B);
        }
    },
    Union() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.union(T,B);
        }
    },
    Intersection() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.intersection(T,B);
        }
    },
    Difference() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.difference(T,B);
        }
    },
    Analogy() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.analogy(T,B);
        }
    },
    ReduceConjunction() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.reduceConjunction(T,B);
        }
    },
    AnonymousAnalogy() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.anonymousAnalogy(T,B);
        }
    },
    Exemplification() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.exemplification(T,B);
        }
    },
    DecomposeNegativeNegativeNegative() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.decomposeNegativeNegativeNegative(T,B);
        }
    },
    DecomposePositiveNegativePositive() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.decomposePositiveNegativePositive(T,B);
        }
    },
    DecomposeNegativePositivePositive() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.decomposeNegativePositivePositive(T,B);
        }
    },
    DecomposePositiveNegativeNegative() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.decomposePositiveNegativeNegative(T,B);
        }
    },
    Identity() {
        @Override public Truth get(final Truth T, final Truth B) {
            return new DefaultTruth(T.getFrequency(), T.getConfidence());
        }
    }

    ;
    /**
     * @param T taskTruth
     * @param B beliefTruth (possibly null)
     * @return
     */
    abstract public Truth get(Truth T, Truth B);

    public final Truth get(final Truth t) {
        return get(t, null);
    }

    static final Map<Term, TruthFunction> atomToTruthModifier = Global.newHashMap(TruthFunction.values().length);

    static {
        for (TruthFunction tm : TruthFunction.values())
            atomToTruthModifier.put(Atom.the(tm.toString()), tm);
    }

    public static TruthFunction get(Term a) {
        return atomToTruthModifier.get(a);
    }

}
