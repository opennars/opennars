package nars.nal.nal5;

import nars.term.Term;
import nars.term.compound.CompoundN;

/**
 * Common parent class for Conjunction and Disjunction
 */
public abstract class Junction<T extends Term> extends CompoundN<T> {

    @SafeVarargs
    protected Junction(T... arg) {
        super(arg);
    }

}
