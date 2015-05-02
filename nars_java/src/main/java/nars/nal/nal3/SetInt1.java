package nars.nal.nal3;

import nars.nal.NALOperator;
import nars.nal.term.Compound1;
import nars.nal.term.Term;

/** efficient implementation of a set with one element */
public class SetInt1 extends Compound1 implements SetInt, SetTensional {

    protected SetInt1(Term the) {
        super(the);

        init(term);
    }

    @Override
    public NALOperator operator() {
        return NALOperator.SET_INT;
    }

    @Override
    public Term clone() {
        return new nars.nal.nal3.SetInt1(the());
    }

    @Override
    public Term clone(Term[] replaced) {
        return SetInt.make(replaced);
    }

    @Override
    protected CharSequence makeName() {
        return SetTensional.makeSetName(NALOperator.SET_INT_OPENER.ch, NALOperator.SET_INT_CLOSER.ch, the());
    }
}
