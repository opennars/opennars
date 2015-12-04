package nars.nal.nal3;

import nars.term.DefaultCompound2;
import nars.term.Term;

/**
 * Base class for SetInt (intensional set) and SetExt (extensional set), where N>1
 */
abstract public class AbstractSetN<T extends Term> extends DefaultCompound2<T> implements SetTensional<T> {


    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    protected AbstractSetN(final T[] arg) {
        super(arg);
    }



    @Override
    public final boolean isCommutative() {
        return true;
    }

}
