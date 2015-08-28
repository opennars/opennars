package nars.meta.pre;

import nars.meta.TaskRule;
import nars.premise.Premise;
import nars.term.Term;

/** for use with precondtiion term 0 */
final public class MatchTaskTerm extends MatchTerm {

    public MatchTaskTerm(Term pattern, TaskRule rule) {
        super(pattern, rule);
    }

    @Override protected final Term getTerm(Premise p) {
        return p.getTask().getTerm();
    }
}
