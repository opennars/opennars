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


    static Compound make(Term... c) {
        return (Compound) GenericCompound.COMPOUND(Op.SET_EXT, c);

    }

    static Compound make(Collection<Term> c) {
        return make(c.toArray(new Term[c.size()]));
    }


    static Term subtractExt(Compound A, Compound B) {
        if (A.equals(B)) return null; //empty set
        return SetExt.make(SetTensional.subtract(A,B));
    }


//    default void appendCloser(Appendable p) throws IOException {
//        p.append(Symbols.SET_EXT_CLOSER);
//    }
}
