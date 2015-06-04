package nars.nal.nal3;

import nars.nal.NALOperator;
import nars.nal.Terms;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import java.util.Collection;

/**
 * Created by me on 5/2/15.
 */
public interface SetExt extends SetTensional {

    @Override
    default public NALOperator operator() {
        return NALOperator.SET_EXT;
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

}
