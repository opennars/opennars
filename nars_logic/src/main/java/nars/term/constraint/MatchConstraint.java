package nars.term.constraint;

import nars.term.Term;
import nars.term.transform.FindSubst;


@FunctionalInterface
public interface MatchConstraint {
    /**
     *
     * @param assignee X variable
     * @param value Y value
     * @param f match context
     * @return true if match is INVALID, false if VALID (reversed)
     */
    boolean invalid(Term assignee, Term value, FindSubst f);
}
