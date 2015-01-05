package nars.operator.io;

import nars.core.control.AbstractTask;

/**
 * Resets memory, @see memory.reset()
 */
public class Reset extends AbstractTask<CharSequence> {

    public Reset() {        
        super();
    }

    @Override
    public CharSequence name() {
        return "Reset";
    }
    
}
