package nars.nal.nal3;

import nars.Op;
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
    public Op operator() {
        return Op.SET_EXT;
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
        p.append(Op.SET_EXT_CLOSER.ch);
    }



}
