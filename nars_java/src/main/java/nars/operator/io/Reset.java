package nars.operator.io;

import nars.logic.entity.AbstractTask;

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
