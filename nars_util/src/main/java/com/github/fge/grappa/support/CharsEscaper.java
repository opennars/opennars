package com.github.fge.grappa.support;


import com.google.common.escape.Escaper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class CharsEscaper
    extends Escaper
{
    private static final ArrayBasedCharEscaper DELEGATE
        = new ArrayBasedCharEscaper(Chars.escapeMap(), Character.MIN_VALUE,
            Character.MAX_VALUE)
    {
        @Override
        protected char[] escapeUnsafe(char c)
        {
            return new char[] { c };
        }
    };

    public static final Escaper INSTANCE = new CharsEscaper();

    private CharsEscaper()
    {
    }

    @Override
    public String escape(String string)
    {
        return DELEGATE.escape(string.replace("\r\n", "\n"));
    }
}
