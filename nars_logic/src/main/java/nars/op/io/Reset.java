package nars.op.io;

import nars.Memory;
import nars.NAR;
import nars.nal.nal8.ImmediateOperation;

/**
 * Resets memory, @see memory.reset()
 */
public class Reset extends ImmediateOperation {


    private final NAR nar;

    public Reset(NAR n) {
        super();

        this.nar = n;

    }

    @Override
    public String toString() {
        return "Reset";
    }

    @Override
    public void execute(Memory m) {
        nar.reset();
    }
}
