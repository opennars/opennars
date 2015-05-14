package nars.io;

import java.util.function.Consumer;

/** A low-level handler for parsing text.
 *  Named reaction because it is analogous to an automatic reflex or reaction. */
public interface TextReaction<A> extends ObjectReaction<String, A> {
    
    /**
     * 
     * @param nar reasoner
     * @param input text to input
     * @param sense
     * @return either:
     *      True meaning the input was handled and handle no others, 
     *      Task instance, 
     *      a new input task, or null
     */
    @Override
    public void react(String input, Consumer<A> sense);
}
