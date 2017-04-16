package nars.io;

import nars.core.NAR;

/** A low-level handler for parsing text.  
 *  Named reaction because it is analogous to an automatic reflex or reaction. */
public interface TextReaction {
    
    /**
     * 
     * @param nar reasoner
     * @param input text to input
     * @param lastHandler a previously invoked handler which returned true, or null if none had reacted yet
     * @return 
     */
    public boolean react(NAR nar, String input, TextReaction lastHandler);
}
