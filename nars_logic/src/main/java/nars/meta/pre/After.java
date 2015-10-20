package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.process.ConceptProcess;
import nars.task.Task;

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
        return getClass().getSimpleName() + '[' + taskBeforeBelief + ']';
    }

    @Override
    public boolean test(RuleMatch m) {
        final ConceptProcess premise = m.premise;

        if (!premise.isTaskAndBeliefEvent())
            return false;

        final Task task = premise.getTask();
        final Task belief = premise.getBelief();

        int pdur = premise.duration();

        if (!taskBeforeBelief) {
            return task.startsAfter(belief, pdur);
        }
        else {
            return belief.startsAfter(task, pdur);
        }
    }
}
