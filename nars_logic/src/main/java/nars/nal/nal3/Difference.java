package nars.nal.nal3;

import nars.term.Term;
import nars.term.compound.CompoundN;

/**
 * Common parent class for DifferenceInt and DifferenceExt
 */
public abstract class Difference extends CompoundN {

    Difference(Term a, Term b) {
        super(a, b);
    }

//    public static void ensureValidDifferenceSubterms(Term[] arg) {
//        if ((arg.length  != 2) || (arg[0].equals(arg[1]))) {
//            throw new RuntimeException("invalid difference subterms");
//        }
//    }

    @Override public final boolean isCommutative() {
        return false;
    }

}
