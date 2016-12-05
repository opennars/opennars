package nars.io;

import nars.entity.Item;


/**
 * Input perception command to queue 'stepLater' cycles in Memory
 * TODO wrap as Operator
 */
public class PauseInput extends Item<CharSequence> {
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
