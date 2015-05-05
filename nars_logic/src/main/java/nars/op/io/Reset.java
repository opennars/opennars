package nars.op.io;

import nars.Memory;
import nars.nal.nal8.ImmediateOperation;

/**
 * Resets memory, @see memory.reset()
 */
public class Reset extends ImmediateOperation {

    private final boolean hard;

    public Reset(boolean hard) {
        super();
        this.hard = hard;
    }

    @Override
    public String toString() {
        return "Reset[" + (hard ? "hard" : "soft") + ']';
    }

    @Override
    public void execute(Memory m) {
        m.reset(hard);
    }
}
