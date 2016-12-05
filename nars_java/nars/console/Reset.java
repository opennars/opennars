package nars.console;

import nars.core.control.AbstractTask;

/**
 * Resets memory, @see memory.reset()
 */
public class Reset extends AbstractTask<CharSequence> {

    public String input;
    public Reset(String input) {        
        super();
        this.input=input;
    }

    @Override
    public CharSequence name() {
        return "Reset";
    }
    
}
