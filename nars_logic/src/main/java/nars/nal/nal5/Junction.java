package nars.nal.nal5;

import nars.term.CompoundN;
import nars.term.Term;

/**
 * Common parent class for Conjunction and Disjunction
 */
abstract public class Junction<T extends Term> extends CompoundN<T> {

    protected Junction(Term[] arg) {
        super(arg);
    }

}
