package nars.nal.op;

import nars.Op;
import nars.nal.PremiseAware;
import nars.nal.PremiseMatch;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compile.TermBuilder;
import nars.term.compound.Compound;
import nars.term.transform.MapSubst;
import nars.term.transform.Subst;


public class substitute extends ImmediateTermTransform implements PremiseAware {

    public static final Atom INDEP_VAR = Atom.the("$", true);
    public static final Atom QUERY_VAR = Atom.the("?", true);
    public static final Atom DEP_VAR = Atom.the("#", true);

    @Override
    public Term function(Compound x, TermBuilder i) {
        throw new RuntimeException("n/a");
    }

    @Override
    public Term function(Compound p, PremiseMatch r) {
        final Term[] xx = p.terms();

        //term to possibly transform
        final Term term = xx[0];

        //original term (x)
        final Term x = xx[1];

        //replacement term (y)
        final Term y = xx[2];

        return subst(r, term, x, y);
    }

    public static Term resolve(PremiseMatch r, Term x) {
        Term x2 = r.yx.get(x);
        if (x2 == null)
            x2 = x;
        return x2;
    }

    public static Term subst(PremiseMatch r, Term term, Term x, Term y) {
        if (x.equals(y))
            return term;

        x = resolve(r, x);
        y = resolve(r, y);

        MapSubst m = new MapSubst(r.yx);
        m.xy.put(x, y);

        return subst(r, m, term);
    }

    public static Term subst(PremiseMatch r, Subst m, Term term) {
        return subst(r.premise.memory().index, m, term);
    }

    public static Term subst(TermBuilder i, Subst m, Term term) {
        return i.apply(m, term);
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
