package nars.io;

/**
 *
 * Implements a way to process perceived input objects
 * into nars events, usually Tasks but can also include Exceptions
 * and possibly other types.
 */
public interface ObjectReaction<I> {

    public Object react(I input);

}
