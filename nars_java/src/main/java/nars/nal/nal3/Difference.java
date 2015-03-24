package nars.nal.nal3;

import nars.nal.entity.Compound;
import nars.nal.entity.Term;

/**
 * Common parent class for DifferenceInt and DifferenceExt
 */
abstract public class Difference extends Compound {

    public Difference(Term[] arg) {
        super(arg);
    }
}
