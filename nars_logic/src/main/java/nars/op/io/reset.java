package nars.op.io;

import nars.nal.nal8.ImmediateOperator;
import nars.nal.nal8.Operation;
import nars.task.Task;

/**
 * Resets memory, @see memory.reset()
 */
public class reset extends ImmediateOperator {

    public reset() {
        super();
    }

    public Task reset() { return newTask(newOperation()); }

    @Override
    public String toString() {
        return "Reset";
    }

    @Override
    public void accept(Task<Operation> o) {
        nar.memory().clear();
        //nar.reset()
    }
}
