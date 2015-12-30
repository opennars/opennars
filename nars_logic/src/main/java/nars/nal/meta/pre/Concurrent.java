package nars.nal.meta.pre;

import nars.Premise;
import nars.nal.PremiseMatch;
import nars.nal.meta.BooleanCondition;
import nars.nal.nal7.Tense;
import nars.task.Task;

/**
 * Created by me on 8/15/15.
 */
public class Concurrent extends BooleanCondition {

    public static final Concurrent the = new Concurrent();

    protected Concurrent() {
    }

    @Override
    public final String toString() {
        return "concurrent"; //getClass().getSimpleName();
    }

    @Override
    public final boolean eval(PremiseMatch m) {
        Premise premise = m.premise;

        if (!premise.isEvent())
            return false;

        Task task = premise.getTask();
        Task belief = premise.getBelief();

        //return task.concurrent(belief, m.premise.duration());
        return Tense.overlaps(task, belief);
    }

}
