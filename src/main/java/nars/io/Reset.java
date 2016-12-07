package nars.io;

import nars.entity.Item;

/**
 * Resets memory, @see memory.reset()
 */
public class Reset extends Item<CharSequence> {

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
