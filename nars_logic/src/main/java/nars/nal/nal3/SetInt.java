package nars.nal.nal3;

import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;

import java.util.Collection;

/**
 * Created by me on 5/2/15.
 */
public interface SetInt<T extends Term> extends SetTensional<T> {

    static Compound make(final Term... t) {
        switch (t.length) {
            case 0: throw new RuntimeException("Set requires >1 terms");
            case 1: return new SetInt1(t[0]);
            default: return new SetIntN(Terms.toSortedSetArray(t));
        }
    }

    static Compound make(Collection<Term> t) {
        switch (t.size()) {
            case 0: throw new RuntimeException("empty set");
            case 1: return new SetInt1(t.iterator().next());
            default: return new SetIntN( Terms.toSortedSetArray(t) );
        }
    }
//    default void appendCloser(Writer p) throws IOException {
//        p.append(Symbols.SET_INT_CLOSER);
//    }

}
