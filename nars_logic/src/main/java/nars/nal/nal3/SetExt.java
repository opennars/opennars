package nars.nal.nal3;

import nars.Op;
import nars.term.Terms;
import nars.term.Compound;
import nars.term.Term;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

/**
 * Created by me on 5/2/15.
 */
public interface SetExt extends SetTensional {

    @Override
    default public Op operator() {
        return Op.SET_EXT;
    }


    public static Compound make(Term... t) {
        t = Terms.toSortedSetArray(t);
        switch (t.length) {
            case 0: return null;
            case 1: return new SetExt1(t[0]);
            default: return new SetExtN(t);
        }
    }

    public static Compound make(Collection<Term> l) {
        return make(l.toArray(new Term[l.size()]));
    }

    default void appendCloser(Writer p) throws IOException {
        p.append(Op.SET_EXT_CLOSER.ch);
    }
}
