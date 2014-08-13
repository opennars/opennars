package nars.core.task;

import nars.entity.AbstractTask;

/**
 * Input perception command to queue 'stepLater' cycles in Memory
 */
public class PauseInput extends AbstractTask {
    public final int cycles;

    public PauseInput(int cycles) {        
        super();
        this.cycles = cycles;
    }

    @Override
    public CharSequence getKey() {
        return "PuaseInput(" + cycles + ')';
    }
    
    
    
}
