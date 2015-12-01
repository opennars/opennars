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

    public static final After the = new After();

    protected After() {
        super();
    }

    @Override
    public String toString() {
        return "After";
    }

    @Override
    public final boolean test(RuleMatch m) {
        final Premise premise = m.premise;

        if (!premise.isEvent())
            return false;

        final Task task = premise.getTask();
        final Task belief = premise.getBelief();

        return task.startsAfter(belief/*, premise.duration()*/);
    }
}
