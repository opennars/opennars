package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.meta.pre.PreCondition1;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class NotImplicationOrEquivalence extends PreCondition1 {

    public NotImplicationOrEquivalence(Term arg1) {
        super(arg1);
    }

    @Override
    public boolean test(RuleMatch m, Term arg1) {
        final Task task = m.premise.getTask();
        final Task belief = m.premise.getBelief();

        if (arg1.equals(task.getTerm())) {
            if (task.getTerm() instanceof Implication || task.getTerm() instanceof Equivalence) {
                return false;
            }
        }
        if (belief != null && arg1.equals(belief.getTerm())) {
            if (belief.getTerm() instanceof Implication || belief.getTerm() instanceof Equivalence) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEarly() {
        return true;
    }
}
