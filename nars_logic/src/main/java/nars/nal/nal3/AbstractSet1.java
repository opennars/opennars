package nars.nal.nal3;

import nars.term.Compound1;
import nars.term.Term;

/**
 * Created by me on 6/2/15.
 */
abstract public class AbstractSet1<T extends Term> extends Compound1<T> implements SetTensional {

    public AbstractSet1(T the) {
        super(the);
    }


    @Override
    public boolean appendTermOpener() {
        return false;
    }
}
