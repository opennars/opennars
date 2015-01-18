package nars.logic.nal3;

import nars.logic.entity.CompoundTerm;
import nars.logic.entity.Term;

/**
 * Common parent class for DifferenceInt and DifferenceExt
 */
abstract public class Difference extends CompoundTerm {

    public Difference(Term[] arg) {
        super(arg);
    }
}
