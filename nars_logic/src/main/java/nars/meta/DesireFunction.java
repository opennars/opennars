package nars.meta;

import nars.Global;
import nars.term.Atom;
import nars.term.Term;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Created by me on 8/1/15.
 */
public enum DesireFunction {

    Negation() {
        @Override public Truth get(final Truth T, final Truth B) { return TruthFunctions.negation(T); }
    },
    Strong() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.desireStrong(T,B);
        }
    },
    Weak() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.desireWeak(T, B);
        }
    },
    Induction() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.desireInd(T,B);
        }
    },
    Deduction() {
        @Override public Truth get(final Truth T, final Truth B) {
            return TruthFunctions.desireDed(T,B);
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
    abstract public Truth get(Truth T, @Nullable Truth B);

    public final Truth get(final Truth t) {
        return get(t, null);
    }

    static final Map<Term, DesireFunction> atomToTruthModifier = Global.newHashMap(DesireFunction.values().length);

    static {
        for (DesireFunction tm : DesireFunction.values())
            atomToTruthModifier.put(Atom.the(tm.toString()), tm);
    }

    public static DesireFunction get(Term a) {
        return atomToTruthModifier.get(a);
    }

}
