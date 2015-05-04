package nars.nal.nal3;

import nars.nal.NALOperator;
import nars.nal.term.Compound1;
import nars.nal.term.Term;

/** efficient implementation of a set with one element */
public class SetExt1 extends Compound1 implements SetExt, SetTensional {

    protected SetExt1(Term the) {
        super(the);

        init(term);
    }

    @Override
    public NALOperator operator() {
        return NALOperator.SET_EXT;
    }

    @Override
    public Term clone() {
        return new SetExt1(the());
    }

    @Override
    public Term clone(Term[] replaced) {
        return SetExt.make(replaced);
    }

    @Override
    protected CharSequence makeName() {
        return SetTensional.makeSetName(NALOperator.SET_EXT_OPENER.ch, NALOperator.SET_EXT_CLOSER.ch, the());
    }

    @Override
    protected byte[] makeKey() {
        return SetTensional.makeKey(NALOperator.SET_EXT_OPENER.ch, NALOperator.SET_EXT_CLOSER.ch, the());
    }

}
