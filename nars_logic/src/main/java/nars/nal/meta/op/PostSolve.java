package nars.nal.meta.op;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.term.Term;
import nars.term.transform.FindSubst;

/**
 * Called after 1 or more after conclusions have completed to apply their results to a rederived term
 */
public final class PostSolve extends PreCondition {

    public static final PostSolve the = new PostSolve();

    protected PostSolve() {
        super();
    }


    @Override
    public String toString() {
        return "PostSolve";
    }

    @Override
    public boolean test(RuleMatch m) {

        Term dt = m.derived.get();

        FindSubst.VarCachedVersionMap secondary = m.secondary;
        if (!secondary.isEmpty()) {
            Term rederivedTerm = dt.apply(secondary, true);

            //its possible that the substitution produces an invalid term, ex: an invalid statement
            dt = rederivedTerm;
        }

        m.derived.set(dt);
        return true;
    }

}
