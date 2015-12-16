package nars.nal.meta.op;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.term.Term;
import nars.term.transform.FindSubst;

/**
 * Called after 1 or more after conclusions have completed to apply their results to a rederived term
 */
public final class ReSolve extends PreCondition {

    public static final ReSolve the = new ReSolve();

    protected ReSolve() {
    }


    @Override
    public String toString() {
        return "ReSolve";
    }

    @Override
    public boolean test(RuleMatch match) {

        Term dt = match.derived.get();

        FindSubst.VarCachedVersionMap secondary = match.secondary;

        if (!secondary.isEmpty()) {

            Term rederivedTerm = dt.apply(secondary, true);

            //its possible that the substitution produces an invalid term, ex: an invalid statement
            dt = rederivedTerm;
        }
        if (dt == null) return false;

        match.derived.set(dt);
        return true;
    }

}
