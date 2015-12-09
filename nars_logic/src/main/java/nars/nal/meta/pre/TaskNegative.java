package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.nal.meta.PostCondition;
import nars.nal.meta.PreCondition;
import nars.task.Task;

/**
 * Created by me on 8/15/15.
 */
public final class TaskNegative extends PreCondition {

    public TaskNegative() {
    }

    @Override
    public boolean test(RuleMatch m) {
        Task task = m.premise.getTask();
        return (task.isJudgmentOrGoal() && task.getFrequency() < PostCondition.HALF);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }


}
