package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class IsEvent extends PreCondition2 {

    public IsEvent(Term var1, Term var2) {
        super(var1, var2);
    }


    @Override
    public boolean test(final RuleMatch m, Term a, Term b) {
        Task task = m.premise.getTask();
        if (b == null) {
            return false;
        }
        if (a.equals(task.getTerm()) && task.getOccurrenceTime() == Stamp.ETERNAL) {
            return false;
        }

        Sentence belief = m.premise.getBelief();
        return !(b.equals(belief.getTerm()) && (belief == null || belief.getOccurrenceTime() == Stamp.ETERNAL));

    }
}
