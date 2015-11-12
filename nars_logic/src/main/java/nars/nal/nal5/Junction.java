package nars.nal.nal5;

import nars.term.DefaultCompound2;
import nars.term.Term;

/**
 * Common parent class for Conjunction and Disjunction
 */
abstract public class Junction<T extends Term> extends DefaultCompound2<T> {

    protected Junction() {
        super();
    }

}
