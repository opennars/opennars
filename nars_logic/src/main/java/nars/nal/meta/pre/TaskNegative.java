package nars.nal.meta.pre;

import nars.nal.PremiseMatch;
import nars.nal.meta.BooleanCondition;
import nars.nal.meta.PostCondition;
import nars.task.Task;

/**
 * Created by me on 8/15/15.
 */
public final class TaskNegative extends BooleanCondition<PremiseMatch> {

    @Override
    public boolean booleanValueOf(PremiseMatch m) {
        Task task = m.premise.getTask();
        return (task.isJudgmentOrGoal() && task.getFrequency() < PostCondition.HALF);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }


}
