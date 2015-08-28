package nars.meta.pre;

import nars.meta.TaskRule;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.Term;

/** constructed for precondition term 1 */
final public class MatchBeliefTerm extends MatchTerm {

    public MatchBeliefTerm(Term pattern, TaskRule rule) {
        super(pattern, rule);
    }

    @Override protected final Term getTerm(final Premise p) {

        final Task tl = p.getBelief();

        if (tl == null) return null;

        return tl.getTerm();
    }
}
