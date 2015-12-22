package nars.nal.op;

import nars.Op;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.term.transform.FindSubst;
import nars.term.transform.MapSubst;
import nars.util.data.random.XorShift128PlusRandom;

import java.util.Random;

/** substituteIfUnifies(term, variableType, varFrom, varTo) */
public final class substituteIfUnifies extends substitute {

    public static final Atom INDEP_VAR = Atom.the("$", true);
    public static final Atom QUERY_VAR = Atom.the("?", true);
    public static final Atom DEP_VAR = Atom.the("#", true);

    @Override public Term function(Compound p) {
        final Term[] xx = p.terms();
        final Term term = xx[0];
        final Term x = xx[2];
        final Term y = xx[3];

        return super.subst(p, term, x, y);
    }

    @Override
    protected boolean substitute(Compound p, MapSubst m, Term a, Term b) {
        final Term type = p.term(1);
        Op o;

        //TODO cache the type
        if (type.equals(INDEP_VAR)) o = Op.VAR_INDEP;
        else if (type.equals(DEP_VAR)) o = Op.VAR_DEP;
        else if (type.equals(QUERY_VAR)) o = Op.VAR_QUERY;
        //...else
        else
            throw new RuntimeException("unrecognizd subst type: " + type);


        Random rng = new XorShift128PlusRandom(1);

        FindSubst sub = new FindSubst(o, rng) {
            @Override public boolean onMatch() {
                return false;
            }
        };

        boolean result = sub.match(a, b);
        m.subs.putAll(sub.xy); //HACK, use the same map instance
        return result;

//        boolean result;
//        if (sub.match(a, b)) { //matchAll?
//            //m.secondary.putAll(sub.xy);
//            result = true;
//        }
//        else {
//            result = false;
//        }
//
//        return result;
    }
}
