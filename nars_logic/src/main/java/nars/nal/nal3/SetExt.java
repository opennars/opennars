package nars.nal.nal3;

import nars.Op;
import nars.term.Term;
import nars.term.Terms;
import nars.term.compound.Compound;

import java.util.Collection;

/**
 * Created by me on 5/2/15.
 */
public interface SetExt<T extends Term> extends SetTensional<T> {

    @Override
    default Op op() {
        return Op.SET_EXT;
    }


    static Compound make(final Collection<Term> c) {
        return SetExt.make(c.toArray(new Term[c.size()]));
    }

    static Compound make(final Term... t) {
        switch (t.length) {
            case 0: throw new RuntimeException("empty set");
            default: return new SetExtN( Terms.toSortedSetArray(t));
        }
    }

//    static Compound make(Collection<Term> t) {
//        switch (t.size()) {
//            case 0: throw new RuntimeException("empty set");
//            case 1: return new SetExt1(t.iterator().next());
//            default: return new SetExtN( Terms.toSortedSetArray(t) );
//        }
//    }

    static Term subtract(SetExt A, SetExt B) {
        if (A.equals(B)) return null; //empty set
        return SetExt.make(SetTensional.subtract(A,B));
    }


//    default void appendCloser(Appendable p) throws IOException {
//        p.append(Symbols.SET_EXT_CLOSER);
//    }
}
