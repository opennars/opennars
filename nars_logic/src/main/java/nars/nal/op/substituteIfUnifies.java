package nars.nal.op;

import nars.Op;
import nars.nal.RuleMatch;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.term.transform.FindSubst;

/** substituteIfUnifies(term, variableType, varFrom, varTo) */
public final class substituteIfUnifies extends ImmediateTermTransform {

    public static final Atom INDEP_VAR = Atom.the("$", true);
    public static final Atom QUERY_VAR = Atom.the("?", true);
    public static final Atom DEP_VAR = Atom.the("#", true);

    @Override public Term function(Compound x) {
        final Term[] xx = x.terms();
        final Term term = xx[0];
        final Term type = xx[1];
        final Term from = xx[2];
        final Term to = xx[3];


        //TODO

        return term;
    }

    protected boolean substitute(Op type, RuleMatch m, Term a, Term b) {
        FindSubst sub = new FindSubst(type, m.premise.getRandom()) {
            @Override
            public boolean onMatch() {
                return false;
            }
        };

        boolean result;
        if (sub.match(a, b)) { //matchAll?
            m.secondary.putAll(sub.xy);
            result = true;
        }
        else {
            result = false;
        }

        return result;
    }
}
