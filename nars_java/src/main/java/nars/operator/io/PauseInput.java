package nars.operator.io;

import nars.core.Memory;
import nars.logic.nal8.ImmediateOperation;

/**
 * Input perception command to queue 'stepLater' cycles in Memory
 * TODO wrap as Operator
 */
public class PauseInput extends ImmediateOperation {
    public final int cycles;

    public PauseInput(int cycles) {        
        super();
        this.cycles = cycles;
    }

    @Override
    public CharSequence name() {
        return "PauseInput(" + cycles + ')';
    }


    @Override
    public void execute(Memory m) {
        m.stepLater(cycles);
    }
}
