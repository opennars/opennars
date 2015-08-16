package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.meta.pre.PreCondition1;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class Concurrent extends PreCondition1 {

    public Concurrent(Term arg1) {
        super(arg1);
    }

    @Override
    public boolean test(RuleMatch m, Term arg1) {
        final Task task = m.premise.getTask();
        final Task belief = m.premise.getBelief();

        if (belief == null) {
            return false;
        }
        if (task.isEternal() || belief.isEternal()) {
            return false;
        }
        long time1 = 0, time2 = 0;
        final int dur = m.premise.duration();
        if (arg1.equals(task.getTerm())) {
            if (!(!task.after(belief, dur) && !belief.after(task, dur)))
                return false;
        }
        if (arg1.equals(belief.getTerm())) {
            if (!(!task.after(belief, dur) && !belief.after(task, dur)))
                return false;
        }
        return true;
    }
}
