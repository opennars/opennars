package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class After extends AbstractMeasureTime {
    public After(Term arg1, Term arg2, Term[] args) {
        super(arg1, arg2, args[2]);
    }

    @Override
    protected boolean test(RuleMatch m, Term arg1, Term arg2, long time1, long time2, Term c) {
        final Task task = m.premise.getTask();
        final Task belief = m.premise.getBelief();
        int dur = m.premise.duration();
        if (arg1.equals(task.getTerm())) {
            if (!task.after(belief, dur))
                return false;
        }
        if (arg1.equals(belief.getTerm())) {
            if (!belief.after(task, dur))
                return false;
        }
        return true;
    }
}
