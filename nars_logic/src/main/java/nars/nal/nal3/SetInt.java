package nars.nal.nal3;

import nars.term.Term;
import nars.term.Terms;
import nars.term.compound.Compound;

import java.util.Collection;

/**
 * Created by me on 5/2/15.
 */
public interface SetInt<T extends Term> extends SetTensional<T> {

    static Compound make(final Collection<Term> c) {
        return SetInt.make(c.toArray(new Term[c.size()]));
    }

    static Compound make(final Term... t) {
        switch (t.length) {
            case 0: throw new RuntimeException("empty set");
            default: return new SetIntN(Terms.toSortedSetArray(t));
        }
    }

//    static Compound make(List<Term> t) {
//        switch (t.size()) {
//            case 0: throw new RuntimeException("empty set");
//            case 1: return new SetInt1(t.get(0));
//            default:
//                return new SetIntN( Terms.toSortedSetArray(t) );
//        }
//    }
//
//    static Compound make(Collection<Term> t) {
//        switch (t.size()) {
//            case 0: throw new RuntimeException("empty set");
//            case 1: return new SetInt1(t.iterator().next());
//            default:
//                return new SetIntN( Terms.toSortedSetArray(t) );
//        }
//    }

    static Term subtract(SetInt A, SetInt B) {
        if (A.equals(B)) return null; //empty set
        return SetInt.make(SetTensional.subtract(A,B));
    }


//    default void appendCloser(Writer p) throws IOException {
//        p.append(Symbols.SET_INT_CLOSER);
//    }

}
