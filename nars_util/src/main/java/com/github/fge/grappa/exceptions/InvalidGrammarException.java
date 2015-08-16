package com.github.fge.grappa.exceptions;

/**
 * Exception thrown when an invalid combination of rules in a grammar is detected
 * at build time
 */
public final class InvalidGrammarException
    extends GrappaException
{
    public InvalidGrammarException(final Throwable cause)
    {
        super(cause);
    }

    public InvalidGrammarException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public InvalidGrammarException(final String message)
    {
        super(message);
    }
}
