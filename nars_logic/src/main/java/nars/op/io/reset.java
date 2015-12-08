package nars.op.io;

import nars.nal.nal8.ImmediateOperator;
import nars.task.Task;

/**
 * Resets memory, @see memory.reset()
 */
public class reset extends ImmediateOperator {

    public reset() {
        super();
    }

    @Override
    public String toString() {
        return "Reset";
    }

    @Override
    public void accept(Task o) {
        nar.memory.clear();
        //nar.reset()
    }
}
