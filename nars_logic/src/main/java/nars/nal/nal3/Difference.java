package nars.nal.nal3;

import nars.term.DefaultCompound;
import nars.term.Term;

/**
 * Common parent class for DifferenceInt and DifferenceExt
 */
abstract public class Difference extends DefaultCompound {

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
