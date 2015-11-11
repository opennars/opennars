package nars.nal.nal3;

import nars.term.DefaultCompound;
import nars.term.Term;

/**
 * Base class for SetInt (intensional set) and SetExt (extensional set), where N>1
 */
abstract public class AbstractSetN<T extends Term> extends DefaultCompound<T> implements SetTensional<T> {


    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    protected AbstractSetN(final T[] arg) {
        super();

        init(arg);
    }

    @Override
    public final T[] terms() {
        return this.term;
    }

    @Override
    public final boolean isCommutative() {
        return true;
    }

}
