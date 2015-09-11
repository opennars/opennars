package nars.op.io;

import nars.NAR;
import nars.nal.nal8.ImmediateOperator;
import nars.nal.nal8.Operation;
import nars.task.Task;

/**
 * Resets memory, @see memory.reset()
 */
public class reset extends ImmediateOperator {

    private NAR nar = null;

    public reset() {
        super();

        //this.nar = n;

    }

    public Task reset() { return newTask(); }

    @Override
    public String toString() {
        return "Reset";
    }

    @Override
    public void accept(Operation o) {
        o.getMemory().clear();
        //nar.reset()
    }
}
