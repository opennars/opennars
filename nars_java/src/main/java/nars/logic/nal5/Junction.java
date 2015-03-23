package nars.logic.nal5;

import nars.logic.entity.Compound;
import nars.logic.entity.Term;

/**
 * Common parent class for Conjunction and Disjunction
 */
abstract public class Junction extends Compound {

    public Junction(final Term[] arg) {
        super(arg);
    }

}
