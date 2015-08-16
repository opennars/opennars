package nars.meta.pre;

import nars.meta.PostCondition;
import nars.meta.RuleMatch;
import nars.meta.pre.PreCondition2;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class IsNegative extends PreCondition2 {

    public IsNegative(Term var1, Term var2) {
        super(var1, var2);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b) {
        Task task = m.premise.getTask();
        Task belief = m.premise.getBelief();
        if (a.equals(task.getTerm()) && task.truth.getFrequency() >= PostCondition.HALF) {
            return false;
        } else if (b.equals(belief.getTerm()) && (belief == null || belief.truth.getFrequency() >= PostCondition.HALF)) {
            return false;
        }
        m.single = true;
        return true;
    }

}
