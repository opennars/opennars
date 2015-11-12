package nars.nal.nal3;

import nars.term.DefaultCompound2;
import nars.term.Term;

/**
 * Common parent class for DifferenceInt and DifferenceExt
 */
abstract public class Difference extends DefaultCompound2 {

    Difference() {
        super();
    }

    public static void ensureValidDifferenceSubterms(Term[] arg) {
        if ((arg.length  != 2) || (arg[0].equals(arg[1]))) {
            throw new RuntimeException("invalid differene subterms");
        }
    }

    @Override public final boolean isCommutative() {
        return false;
    }

}
