package nars.nal.op;

import nars.Op;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.transform.FindSubst;
import nars.util.data.random.XorShift128PlusRandom;

import java.util.Random;

/** substituteIfUnifies(term, variableType, varFrom, varTo) */
public final class substituteIfUnifies extends substitute {


    @Override public Term function(Compound p) {
        final Term[] xx = p.terms();
        final Term op = xx[1];
        final Term x = xx[2];
        final Term y = xx[3];
        final Term term = xx[0];

        FindSubst umap = unifies(op, x, y);
        if (umap!=null) {
            umap.putXY(x, y);
            return subst(umap, term);
        }

        return term;
    }

    FindSubst unifies(Term op, Term x, Term y) {
        Op o = getOp(op);
        Random rng = new XorShift128PlusRandom(1);
        FindSubst sub = new FindSubst(o, rng) {
            @Override public boolean onMatch() {
                //should not get called by .match only
                return false;
            }
        };

        if (sub.match(x, y)) {
            return sub;
        }

        return null;
    }
}
