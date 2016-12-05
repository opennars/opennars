package nars.io;

import nars.control.AbstractTask;

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
