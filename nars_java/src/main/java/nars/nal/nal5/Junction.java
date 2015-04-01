package nars.nal.nal5;

import nars.nal.term.Compound;
import nars.nal.term.Term;

/**
 * Common parent class for Conjunction and Disjunction
 */
abstract public class Junction extends Compound {

    public Junction(final Term[] arg) {
        super(arg);
    }

}
