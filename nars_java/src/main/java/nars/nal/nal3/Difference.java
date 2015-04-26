package nars.nal.nal3;

import nars.nal.term.DefaultCompound;
import nars.nal.term.Term;

/**
 * Common parent class for DifferenceInt and DifferenceExt
 */
abstract public class Difference extends DefaultCompound {

    public Difference(Term[] arg) {
        super(arg);
    }
}
