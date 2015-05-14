package nars.io;

import java.util.function.Consumer;

/**
 *
 * Implements a way to process perceived input objects
 * into nars events, usually Tasks but can also include Exceptions
 * and possibly other types.
 */
public interface ObjectReaction<I,O> {

    public void react(I input, Consumer<O> receiver);

}
