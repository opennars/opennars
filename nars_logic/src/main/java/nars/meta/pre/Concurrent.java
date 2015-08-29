package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.task.Task;

/**
 * Created by me on 8/15/15.
 */
public class Concurrent extends PreCondition {

    public Concurrent() {
        super();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isEarly() {
        return true;
    }

    @Override
    public boolean test(RuleMatch m) {
        if (!m.premise.isEvent())
            return false;

        final Task task = m.premise.getTask();
        final Task belief = m.premise.getBelief();

        return task.concurrent(belief, m.premise.duration());
    }

}
