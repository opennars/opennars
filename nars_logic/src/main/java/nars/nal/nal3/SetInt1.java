package nars.nal.nal3;

import nars.nal.NALOperator;
import nars.nal.term.Compound1;
import nars.nal.term.Term;

import java.io.IOException;
import java.io.Writer;

/** efficient implementation of a set with one element */
public class SetInt1<T extends Term> extends AbstractSet1<T> implements SetInt {

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

    public void appendCloser(Writer p) throws IOException {
        p.append(NALOperator.SET_INT_CLOSER.ch);
    }

}
