package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.meta.TaskRule;
import nars.task.Task;
import nars.term.Term;

/**
 * After(%X,%Y) Means that the task matching the pattern of the first argument is after the task of the pattern of the 2nd
 */
public class After extends PreCondition {


    final boolean taskBeforeBelief;

    public After(boolean taskBeforeBelief) {
        super();
        this.taskBeforeBelief = taskBeforeBelief;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + taskBeforeBelief + "]";
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

        int dur = m.premise.duration();
        if (taskBeforeBelief) {
            return task.after(belief, dur);
        }
        else {
            return belief.after(task, dur);
        }

    }
}
