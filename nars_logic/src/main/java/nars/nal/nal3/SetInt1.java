package nars.nal.nal3;

import nars.nal.NALOperator;
import nars.nal.term.Compound1;
import nars.nal.term.Term;

/** efficient implementation of a set with one element */
public class SetInt1<T extends Term> extends Compound1<T> implements SetInt, SetTensional {

    public SetInt1(T the) {
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

    @Override
    protected byte[] makeKey() {
        return SetTensional.makeKey(NALOperator.SET_INT_OPENER.ch, NALOperator.SET_INT_CLOSER.ch, the());
    }

}
