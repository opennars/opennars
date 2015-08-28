package nars.meta.pre;

import nars.meta.PostCondition;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
final public class TaskNegative extends PreCondition {

    public TaskNegative() {
        super();
    }

    @Override
    public boolean test(final RuleMatch m) {
        final Task task = m.premise.getTask();
        return (task.getFrequency() < PostCondition.HALF);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }


    @Override
    public boolean isEarly() {
        return true;
    }
}
