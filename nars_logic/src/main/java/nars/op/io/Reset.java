package nars.op.io;

import nars.Memory;
import nars.NAR;
import nars.nal.nal8.ImmediateOperator;
import nars.nal.nal8.Operation;

/**
 * Resets memory, @see memory.reset()
 */
public class Reset extends ImmediateOperator {

    private NAR nar = null;

    public Reset() {
        super();

        //this.nar = n;

    }

    @Override
    public String toString() {
        return "Reset";
    }

    @Override
    public void accept(Operation terms) {
        //nar.reset()
    }
}
