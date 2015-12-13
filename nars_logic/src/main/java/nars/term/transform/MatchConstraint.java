package nars.term.transform;

import nars.term.Term;

/**
 * Created by me on 12/13/15.
 */
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
