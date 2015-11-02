package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.nal.nal7.Temporal;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class IsEvent extends PreCondition2 {

    public IsEvent(Term var1) {
        this(var1, null);
    }

    public IsEvent(Term var1, Term var2) {
        super(var1, var2);
    }


    @Override
    public boolean test(final RuleMatch m, Term a, Term b) {
        Premise premise = m.premise;

        Task task = premise.getTask();

        if (!isTemporal(a, task)) {
            return false;
        }

        if (b != null) {
            Task belief = premise.getBelief();
            return isTemporal(b, belief);
        }

        //require both task and belief to be events
        return false;
    }

    public static boolean isTemporal(Term a, Task task) {
        return a.equals(task.getTerm()) && !Temporal.isEternal(task.getOccurrenceTime());
    }
}
