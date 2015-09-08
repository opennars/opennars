package nars.nal.nal3;

import nars.term.Compound;
import nars.term.Term;

/**
 * Common parent class for DifferenceInt and DifferenceExt
 */
abstract public class Difference extends Compound {

    public Difference(Term[] arg) {
        super(arg);
    }
}
