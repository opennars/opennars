package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class After extends PreCondition1 {


    public After(Term arg1) {
        super(arg1);
    }

    @Override
    public boolean test(RuleMatch m, Term a) {
        final Task task = m.premise.getTask();
        final Task belief = m.premise.getBelief();
        int dur = m.premise.duration();
        if (a.equals(task.getTerm())) {
            if (!task.after(belief, dur))
                return false;
        }
        else if (a.equals(belief.getTerm())) {
            if (!belief.after(task, dur))
                return false;
        }
        return true;
    }
}
