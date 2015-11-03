package nars.meta.pre;

import nars.meta.PostCondition;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.task.Task;

/**
 * Created by me on 8/15/15.
 */
final public class TaskNegative extends PreCondition {

    public TaskNegative() {
        super();
    }

    @Override
    public final boolean test(final RuleMatch m) {
        final Task task = m.premise.getTask();
        return (task.isJudgmentOrGoal() && task.getFrequency() < PostCondition.HALF);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName();
    }


}
