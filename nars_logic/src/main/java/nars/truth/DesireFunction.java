package nars.truth;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.nal.meta.TruthOperator;
import nars.term.Term;
import nars.term.atom.Atom;

import java.util.Map;

public enum DesireFunction implements TruthOperator {

    Negation() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            return TruthFunctions.negation(T); }
    },

    Strong() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.desireStrong(T,B);
        }
    },
    Weak() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.desireWeak(T, B);
        }
    },
    Induction() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B == null) return null;
            return TruthFunctions.desireInd(T,B);
        }
    },
    Deduction() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            if (B==null) return null;
            return TruthFunctions.desireDed(T,B);
        }
    },
    Identity() {
        @Override public Truth apply(final Truth T, /* N/A: */ final Truth B, Memory m) {
            return new DefaultTruth(T.getFrequency(), T.getConfidence());
        }
    },
    StructuralStrong() {
        @Override public Truth apply(final Truth T, final Truth B, Memory m) {
            return TruthFunctions.desireStrong(T, newDefaultTruth(m));
        }
    };


    private static Truth newDefaultTruth(Memory m) {
        return m.newDefaultTruth(Symbols.JUDGMENT /* goal? */);
    }


    static final Map<Term, DesireFunction> atomToTruthModifier = Global.newHashMap(DesireFunction.values().length);

    static {
        for (DesireFunction tm : DesireFunction.values())
            atomToTruthModifier.put(Atom.the(tm.toString()), tm);
    }

    @Override
    public final boolean allowOverlap() {
        return false;
    }

    public static DesireFunction get(Term a) {
        return atomToTruthModifier.get(a);
    }

}