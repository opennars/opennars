package nars.nal.nal3;

import nars.term.Compound;
import nars.term.Term;

/**
 * Base class for SetInt (intensional set) and SetExt (extensional set), where N>1
 */
abstract public class AbstractSetN extends Compound implements SetTensional {


    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    protected AbstractSetN(final Term[] arg) {
        super();

        init(arg);
    }

    @Override
    public final Term[] terms() {
        return this.term;
    }

    @Override
    public final boolean isCommutative() {
        return true;
    }

}
