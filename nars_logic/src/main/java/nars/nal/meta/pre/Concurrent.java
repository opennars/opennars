package nars.nal.meta.pre;

import nars.Premise;
import nars.nal.PremiseMatch;
import nars.nal.meta.BooleanCondition;
import nars.nal.nal7.Tense;
import nars.task.Task;

/**
 * Created by me on 8/15/15.
 */
public class Concurrent extends BooleanCondition<PremiseMatch> {

    public static final Concurrent the = new Concurrent();

    protected Concurrent() {
    }

    @Override
    public final String toString() {
        return "concurrent"; //getClass().getSimpleName();
    }

    @Override
    public boolean booleanValueOf(PremiseMatch m) {
        Premise premise = m.premise;

        boolean r = false   ;
        if (premise.isEvent()) {

            Task task = premise.getTask();
            Task belief = premise.getBelief();
            r=Tense.overlaps(task, belief);
        }
        return r;
    }

}
