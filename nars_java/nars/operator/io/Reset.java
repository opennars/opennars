package nars.operator.io;

import nars.entity.AbstractTask;

/**
 * Resets memory, @see memory.reset()
 */
public class Reset extends AbstractTask {

    public Reset() {        
        super();
    }

    @Override
    public CharSequence name() {
        return "Reset";
    }
    
}
