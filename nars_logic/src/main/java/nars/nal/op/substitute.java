package nars.nal.op;

import nars.Op;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;
import nars.term.transform.MapSubst;
import nars.term.transform.Subst;


public class substitute extends ImmediateTermTransform {

    public static final Atom INDEP_VAR = Atom.the("$", true);
    public static final Atom QUERY_VAR = Atom.the("?", true);
    public static final Atom DEP_VAR = Atom.the("#", true);



    @Override public Term function(Compound p, TermIndex i) {
        final Term[] xx = p.terms();

        //term to possibly transform
        final Term term = xx[0];

        //original term (x)
        final Term x = xx[1];

        //replacement term (y)
        final Term y = xx[2];

        return subst(i, term, x, y);
    }

    public static Term subst(TermIndex i, Term term, Term x, Term y) {
        if (x.equals(y))
            return term;

        return subst(i, new MapSubst(x, y), term);
    }

    public static Term subst(TermIndex i, Subst m, Term term) {
        return i.get(m, term);
    }

//    protected boolean substitute(Compound p, MapSubst m, Term a, Term b) {
//        final Term type = p.term(1);
//        Op o = getOp(type);
//
//
//        Random rng = new XorShift128PlusRandom(1);
//
//        FindSubst sub = new FindSubst(o, rng) {
//            @Override public boolean onMatch() {
//                return false;
//            }
//        };
//
//        boolean result = sub.match(a, b);
//        m.subs.putAll(sub.xy); //HACK, use the same map instance
//        return result;
//
////        boolean result;
////        if (sub.match(a, b)) { //matchAll?
////            //m.secondary.putAll(sub.xy);
////            result = true;
////        }
////        else {
////            result = false;
////        }
////
////        return result;
//    }


    public static Op getOp(Term type) {
        Op o;

        //TODO cache the type
        if (type.equals(INDEP_VAR)) o = Op.VAR_INDEP;
        else if (type.equals(DEP_VAR)) o = Op.VAR_DEP;
        else if (type.equals(QUERY_VAR)) o = Op.VAR_QUERY;
            //...else
        else
            throw new RuntimeException("unrecognizd subst type: " + type);
        return o;
    }

}
