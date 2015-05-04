package nars.operate.io;

import nars.Memory;
import nars.nal.nal8.ImmediateOperation;

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
    public String toString() {
        return "PauseInput(" + cycles + ')';
    }


    @Override
    public void execute(Memory m) {
        m.think(cycles);
    }
}
