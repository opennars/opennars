package nars.io;

import nars.control.AbstractTask;

/**
 * Input perception command to queue 'stepLater' cycles in Memory
 * TODO wrap as Operator
 */
public class PauseInput extends AbstractTask<CharSequence> {
    public final int cycles;

    public PauseInput(int cycles) {        
        super();
        this.cycles = cycles;
    }

    @Override
    public CharSequence name() {
        return "PauseInput(" + cycles + ')';
    }
    
    
    
}
