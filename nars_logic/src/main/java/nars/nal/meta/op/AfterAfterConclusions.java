package nars.nal.meta.op;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.term.Term;

import java.util.Map;

/**
 * Called after 1 or more after conclusions have completed to apply their results to a rederived term
 */
public final class AfterAfterConclusions extends PreCondition {

    public final static AfterAfterConclusions the = new AfterAfterConclusions();

    protected AfterAfterConclusions() {
        super();
    }


    @Override
    public String toString() {
        return null;
    }

    @Override
    public boolean test(RuleMatch m) {

        final Map<Term, Term> Outp = m.sub2.outp;

        Term dt = m.post.derivedTerm;

        if (!Outp.isEmpty()) {
            Term rederivedTerm = dt.substituted(Outp);
            Outp.clear();

            //its possible that the substitution produces an invalid term, ex: an invalid statement
            if (rederivedTerm == null)
                return false;

            dt = rederivedTerm;
        }


        //the apply substitute will invoke clone which invokes normalized, so its not necessary to call it here
        return (m.post.derivedTerm = dt.normalized())!=null;
    }
}
