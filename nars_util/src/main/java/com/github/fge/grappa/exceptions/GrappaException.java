package com.github.fge.grappa.exceptions;

/**
 * Base exception class for all grammar/parser errors
 */
public class GrappaException
    extends RuntimeException
{
    public GrappaException(final Throwable cause)
    {
        super(cause);
    }

    public GrappaException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public GrappaException(final String message)
    {
        super(message);
    }
}
