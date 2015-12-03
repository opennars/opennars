package nars.nal.meta.op;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.term.Term;
import nars.term.transform.Substitution;

/**
 * Called after 1 or more after conclusions have completed to apply their results to a rederived term
 */
public final class PostSolve extends PreCondition {

    public final static PostSolve the = new PostSolve();

    protected PostSolve() {
        super();
    }


    @Override
    public String toString() {
        return "PostSolve";
    }

    @Override
    public boolean test(RuleMatch m) {

        final Substitution secondary = m.secondary.get();

        Term dt = m.derived.get();

        if (!secondary.isEmpty()) {
            Term rederivedTerm = dt.substituted(secondary);
            //secondary.clear(); //necessary?

            //its possible that the substitution produces an invalid term, ex: an invalid statement

            dt = rederivedTerm;
        }

        if (dt == null)
            return false;

        //the apply substitute will invoke clone which invokes normalized, so its not necessary to call it here
        Term t = dt.normalized();
        if (t!=null) {
            m.derived.set(t);
            return true;
        }

        return false;
    }
}
