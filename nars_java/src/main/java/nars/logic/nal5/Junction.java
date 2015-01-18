package nars.logic.nal5;

import nars.logic.entity.CompoundTerm;
import nars.logic.entity.Term;

/**
 * Common parent class for Conjunction and Disjunction
 */
abstract public class Junction extends CompoundTerm {

    public Junction(final Term[] arg) {
        super(arg);
    }

}
