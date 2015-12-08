package nars.budget;

import nars.task.Sentence;
import nars.task.Task;

/**
 * Created by me on 11/2/15.
 */
public class TaskAccumulator extends ItemAccumulator<Sentence, Task> {

    public TaskAccumulator(int capacity) {
        super(capacity);
    }
}
