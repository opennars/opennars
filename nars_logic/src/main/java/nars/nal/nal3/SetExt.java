package nars.nal.nal3;

import nars.Op;
import nars.nal.nal1.Inheritance;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by me on 5/2/15.
 */
public interface SetExt<T extends Term> extends SetTensional<T> {

    @Override
    default Op op() {
        return Op.SET_EXT;
    }


    static Compound make(final Term... t) {
        switch (t.length) {
            case 0: return null;
            case 1: return new SetExt1(t[0]);
            default: return new SetExtN( Terms.toSortedSetArray(t));
        }
    }

    static Compound make(Collection<Term> l) {
        return make(l.toArray(new Term[l.size()]));
    }


    /**
     * interprets a SetExt to a set of key,value pairs (Map).
     * ie, it translates this SetExt tp a Map<Term,Term> in the
     * following pattern:
     *
     *      { a:b }  becomes Map a=b
     *      { a:b, b:c } bcomes Map a=b, b=c
     *      { a:b, b:c, c } bcomes Map a=b, b=c, c=c
     *
     * @return
     */
    default Map<Term,Term> toInheritanceMap() {

        Map<Term,Term> result = new HashMap();

        for (Term a : this) {
            if (a.op() == Op.INHERITANCE) {
                Inheritance ii = (Inheritance)a;
                Term subj = ii.getSubject();
                Term pred = ii.getPredicate();
                result.put(subj, pred);
            }
            else {
                //..
            }
        }

        return result;
    }


//    default void appendCloser(Appendable p) throws IOException {
//        p.append(Symbols.SET_EXT_CLOSER);
//    }
}
