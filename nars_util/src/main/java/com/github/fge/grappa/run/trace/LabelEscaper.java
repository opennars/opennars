package com.github.fge.grappa.run.trace;

import com.google.common.escape.ArrayBasedUnicodeEscaper;

import java.util.Collections;

public final class LabelEscaper
    extends ArrayBasedUnicodeEscaper
{
    /*
     * ASCII control, except DEL: 0x00 to 0x1f
     */
    private static final int ASCII_CTL_MAX = 0x1f;
    @SuppressWarnings("HardcodedFileSeparator")
    private static final String[] ASCII_CTL_ESCAPES = {
        "<NUL>",        /* 0x00 */
        "<STH>",
        "<STX>",
        "<ETX>",
        "<EOT>",
        "<ENQ>",
        "<ACK>",
        "<BEL>",
        "\\b",
        "\\t",
        "\\n",
        "<VT>",
        "\\f",
        "\\r",
        "<SO>",
        "<SI>",
        "<DLE>",            /* 0x10 */
        "<DC1>",
        "<DC2>",
        "<DC3>",
        "<DC4>",
        "<NAK>",
        "<SYN>",
        "<ETB>",
        "<CAN>",
        "<EM>",
        "<SUB>",
        "<ESC>",
        "<FS>",
        "<GS>",
        "<RS>",
        "<US>"              /* 0x1f */
    };

    /*
     * ASCII DEL
     */
    private static final int ASCII_DEL = 0x7f;
    private static final String ASCII_DEL_ESCAPE = "<DEL>";

    public LabelEscaper()
    {
        super(Collections.<Character, String>emptyMap(), 0x80,
            Character.MAX_CODE_POINT, null);
    }

    @Override
    protected char[] escapeUnsafe(int cp)
    {
        if (cp <= ASCII_CTL_MAX)
            return ASCII_CTL_ESCAPES[cp].toCharArray();

        if (cp == ASCII_DEL)
            return ASCII_DEL_ESCAPE.toCharArray();

        return null;
    }
}
