package nars.operator.io;

import nars.core.Memory;
import nars.logic.nal8.ImmediateOperation;

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
    public CharSequence name() {
        return "Reset[" + (hard ? "hard" : "soft") + ']';
    }

    @Override
    public void execute(Memory m) {
        m.reset(hard);
    }
}
