package nars.nal.nal5;

import nars.term.Compound;
import nars.term.Term;

/**
 * Common parent class for Conjunction and Disjunction
 */
abstract public class Junction<T extends Term> extends Compound<T> {

    public Junction(final T[] arg) {
        super(arg);
    }


}
