package nars.nal.nal3;

import nars.term.Compound1;
import nars.term.Term;

/**
 * Created by me on 6/2/15.
 */
abstract public class AbstractSet1<T extends Term> extends Compound1<T> implements SetTensional<T> {

    protected AbstractSet1() {
        super();
    }

    @Override
    public final boolean appendTermOpener() {
        return false;
    }

    @Override
    public final Term[] terms() {
        return this.terms.term;
    }

    @Override
    public final boolean isCommutative() {
        return true;
    }
}
