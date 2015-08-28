package nars.meta.pre;

import nars.Op;
import nars.meta.RuleMatch;
import nars.premise.Premise;
import nars.task.Task;

import java.util.EnumMap;

/**
 * Created by me on 8/18/15.
 */
public class TaskTermType extends BeliefTermType {

    public final static EnumMap<Op,TaskTermType> the = new EnumMap(Op.class);

    public static TaskTermType the(final Op o) {
        return the.computeIfAbsent(o, k -> new TaskTermType(k));
    }

    TaskTermType(Op o) {
        super(o);
    }

    @Override
    protected Task getTask(final Premise p) {
        return p.getTask();
    }

}
