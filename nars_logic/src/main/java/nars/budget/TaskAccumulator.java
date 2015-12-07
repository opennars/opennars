package nars.budget;

import nars.task.Sentence;
import nars.task.Task;
import nars.term.compound.Compound;

/**
 * Created by me on 11/2/15.
 */
public class TaskAccumulator<C extends Compound> extends ItemAccumulator<Sentence<C>, Task<C>> {

    public TaskAccumulator(int capacity) {
        super(capacity);
    }
}
