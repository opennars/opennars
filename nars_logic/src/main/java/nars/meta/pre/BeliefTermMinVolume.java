package nars.meta.pre;

import nars.premise.Premise;
import nars.task.Task;

/**
 * Created by me on 8/18/15.
 */
public class BeliefTermMinVolume extends TaskTermMinVolume {

    public BeliefTermMinVolume(int minVolume) {
        super(minVolume);
    }

    @Override
    protected Task getTask(final Premise p) {
        return p.getBelief();
    }
}
