package nars.nal.nal3;

import nars.Op;
import nars.Symbols;
import nars.term.Term;

import java.io.IOException;

/** efficient implementation of a set with one element */
public class SetExt1<T extends Term> extends AbstractSet1<T> implements SetExt<T> {

    public SetExt1(T the) {
        super(the);

        init(term);
    }

    @Override
    public final Op op() {
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

    @Override
    public void appendCloser(Appendable p) throws IOException {
        p.append(Symbols.SET_EXT_CLOSER);
    }

}
