package nars.budget;

import com.gs.collections.api.block.procedure.Procedure2;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;

/**
 * Created by me on 11/2/15.
 */
public class TaskAccumulator<C extends Compound> extends ItemAccumulator<Sentence<C>, Task<C>> {

    public TaskAccumulator(Procedure2<Budget, Budget> merge, int capacity) {
        super(merge, capacity);
        mergePlus();
    }
}
