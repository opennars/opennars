package nars.nal.nal3;

import nars.nal.NALOperator;
import nars.term.Term;

import java.io.IOException;
import java.io.Writer;

/** efficient implementation of a set with one element */
public class SetExt1<T extends Term> extends AbstractSet1<T> implements SetExt {

    public SetExt1(T the) {
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

    public void appendCloser(Writer p) throws IOException {
        p.append(NALOperator.SET_EXT_CLOSER.ch);
    }



}
