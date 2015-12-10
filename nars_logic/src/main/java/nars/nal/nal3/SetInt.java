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

    static Compound make(Collection<Term> c) {
        return SetInt.make(c.toArray(new Term[c.size()]));
    }

    @SafeVarargs
    static Compound make(Term... t) {
        return (Compound)GenericCompound.COMPOUND(Op.SET_INT, t);
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



//    default void appendCloser(Writer p) throws IOException {
//        p.append(Symbols.SET_INT_CLOSER);
//    }

}
