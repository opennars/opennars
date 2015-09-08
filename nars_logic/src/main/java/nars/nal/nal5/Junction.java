package nars.nal.nal5;

import nars.term.DefaultCompound;
import nars.term.Term;

/**
 * Common parent class for Conjunction and Disjunction
 */
abstract public class Junction<T extends Term> extends DefaultCompound<T> {

    public Junction(final T[] arg) {
        super(arg);
    }


}
