package nars.nal.meta.pre;

import nars.Premise;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.task.Task;

/**
 * After(%X,%Y) Means that
 * %X is after %Y
 * TODO use less confusing terminology and order convention
 */
public class After extends PreCondition {

    public After() {
        super();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean test(RuleMatch m) {
        final Premise premise = m.premise;

        if (!premise.isTaskAndBeliefEvent())
            return false;

        final Task task = premise.getTask();
        final Task belief = premise.getBelief();

        int pdur = premise.duration();
        return task.startsAfter(belief, pdur);
    }
}
