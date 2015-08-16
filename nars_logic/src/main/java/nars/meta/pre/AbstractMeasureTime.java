package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.meta.pre.PreCondition3;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
abstract public class AbstractMeasureTime extends PreCondition3 {

    public AbstractMeasureTime(Term var1, Term var2, Term var3) {
        super(var1, var2, var3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {
        final Task task = m.premise.getTask();
        final Task belief = m.premise.getBelief();

        //This part is used commonly, extract into its own precondition
        if (belief == null) {
            return false;
        }
        if (task.isEternal() || belief.isEternal()) {
            return false;
        }

        long time1 = 0, time2 = 0;
        if (a.equals(task.getTerm())) {
            time1 = task.getOccurrenceTime();
            time2 = belief.getOccurrenceTime();
        }
        else if (a.equals(belief.getTerm())) {
            time1 = task.getOccurrenceTime();
            time2 = belief.getOccurrenceTime();
        }

        return test(m, a, b, time1, time2, c);
    }

    protected abstract boolean test(RuleMatch m, Term a, Term b, long time1, long time2, Term c);
}
