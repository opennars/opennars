package com.github.fge.grappa.support;


import com.google.common.escape.Escaper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern ESCAPED = Pattern.compile("\r\n", Pattern.LITERAL);

    private CharsEscaper()
    {
    }

    @Override
    public String escape(String string)
    {
        return DELEGATE.escape(ESCAPED.matcher(string).replaceAll(Matcher.quoteReplacement("\n")));
    }
}
