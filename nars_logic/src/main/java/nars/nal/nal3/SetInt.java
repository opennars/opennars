package nars.nal.nal3;

import nars.Op;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;

import java.util.Collection;

/**
 * Created by me on 5/2/15.
 */
public interface SetInt  {

    static <T extends Term> Compound<T> make(final Collection<T> c) {
        return SetInt.make(c.toArray((T[]) new Term[c.size()]));
    }

    @SafeVarargs
    static <T extends Term> Compound<T> make(final T... t) {
        switch (t.length) {
            case 0: throw new RuntimeException("empty set");
            default:
                return new GenericCompound(Op.SET_INT, t);
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

    static Compound subtractInt(Compound A, Compound B) {
        if (A.equals(B)) return null; //empty set
        return SetInt.make(SetTensional.subtract(A,B));
    }


//    default void appendCloser(Writer p) throws IOException {
//        p.append(Symbols.SET_INT_CLOSER);
//    }

}
