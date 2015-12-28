package nars.op.io;

import nars.nal.nal8.Execution;
import nars.nal.nal8.operator.ImmediateOperator;

/**
 * Resets memory, @see memory.reset()
 */
public class reset extends ImmediateOperator {

    @Override
    public String toString() {
        return "Reset";
    }

    @Override
    public void execute(Execution e) {
        e.nar.memory.clear();
        //nar.reset()
    }
}
