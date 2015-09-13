package nars.nal.nal3;

import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;

import java.util.Collection;

/**
 * Created by me on 5/2/15.
 */
public interface SetInt<T extends Term> extends SetTensional<T> {


    static Compound make(Collection<Term> l) {
        return make(l.toArray(new Term[l.size()]));
    }


    static Compound make(final Term... t) {
        switch (t.length) {
            case 0: return null;
            case 1: return new SetInt1(t[0]);
            default: return new SetIntN(Terms.toSortedSetArray(t));
        }
    }

//    default void appendCloser(Writer p) throws IOException {
//        p.append(Symbols.SET_INT_CLOSER);
//    }

}
