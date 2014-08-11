package nars.io.task;

import nars.entity.Task;

/**
 * Used to queue 'stepLater' cycles in Memory
 */
public class PauseInput extends Task {
    public final int cycles;

    public PauseInput(int cycles) {
        super();
        
        this.cycles = cycles;
    }
    
    
}
