package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.nal.nal7.Temporal;
import nars.process.ConceptProcess;
import nars.task.Task;

/**
 * Created by me on 8/15/15.
 */
public class Concurrent extends PreCondition {

    public Concurrent() {
        super();
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public final boolean test(RuleMatch m) {
        final ConceptProcess premise = m.premise;

        if (!premise.isTaskAndBeliefEvent())
            return false;

        final Task task = premise.getTask();
        final Task belief = premise.getBelief();

        //return task.concurrent(belief, m.premise.duration());
        return Temporal.overlaps(task, belief);
    }

}
