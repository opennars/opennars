package nars.nal.nal3;

import nars.term.DefaultCompound;
import nars.term.Term;

/**
 * Common parent class for DifferenceInt and DifferenceExt
 */
abstract public class Difference extends DefaultCompound {

    public Difference(Term[] arg) {
        super(arg);
    }
}
