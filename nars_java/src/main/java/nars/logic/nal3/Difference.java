package nars.logic.nal3;

import nars.logic.entity.Compound;
import nars.logic.entity.Term;

/**
 * Common parent class for DifferenceInt and DifferenceExt
 */
abstract public class Difference extends Compound {

    public Difference(Term[] arg) {
        super(arg);
    }
}
