package nars.io;

import java.io.Serializable;

/** A low-level handler for parsing text.  
 *  Named reaction because it is analogous to an automatic reflex or reaction. */
public interface TextReaction extends Serializable {
    
    /**
     * 
     * @param nar reasoner
     * @param input text to input
     * @return either:
     *      True meaning the input was handled and handle no others, 
     *      Task instance, 
     *      a new input task, or null
     */
    public Object react(String input);
}
