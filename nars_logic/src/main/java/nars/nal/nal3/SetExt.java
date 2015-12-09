package nars.nal.nal3;

import nars.Op;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;

import java.util.Collection;

/**
 * Created by me on 5/2/15.
 */
public interface SetExt {


    static <T extends Term> Compound<T> make(Collection<T> c) {
        return SetExt.make((T[])c.toArray(new Term[c.size()]));
    }

    @SafeVarargs
    static <T extends Term> Compound<T> make(T... t) {
        switch (t.length) {
            case 0: throw new RuntimeException("empty set");
            default:
                return new GenericCompound(Op.SET_EXT, t);
        }
    }


    static Term subtractExt(Compound A, Compound B) {
        if (A.equals(B)) return null; //empty set
        return SetExt.make(SetTensional.subtract(A,B));
    }


//    default void appendCloser(Appendable p) throws IOException {
//        p.append(Symbols.SET_EXT_CLOSER);
//    }
}
