package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.nal.meta.PostCondition;
import nars.nal.meta.PreCondition;
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
