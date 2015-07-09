package nars.op.io;

import nars.Memory;
import nars.nal.nal8.ImmediateOperation;

/**
 * Resets memory, @see memory.reset()
 */
public class Reset extends ImmediateOperation {


    public Reset() {
        super();
    }

    @Override
    public String toString() {
        return "Reset";
    }

    @Override
    public void execute(Memory m) {
        m.reset(false);
    }
}
