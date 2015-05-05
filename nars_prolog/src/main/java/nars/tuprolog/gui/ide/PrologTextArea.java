/*
 * TextAreaDefaults.java - Encapsulates default values for various settings
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */
package nars.tuprolog.gui.ide;

import nars.tuprolog.gui.edit.*;

import java.awt.*;

public class PrologTextArea extends TextAreaDefaults
{
    public PrologTextArea()
    {
        inputHandler = new DefaultInputHandler();
        inputHandler.addDefaultKeyBindings();
        document = new SyntaxDocument();
        editable = true;

        caretVisible = true;
        caretBlinks = true;
        electricScroll = 3;

        cols = 5;
        rows = 5;
        styles = getSyntaxStyles();
        caretColor = Color.red;
        selectionColor = new Color(0xCCCCFF);
        // lineHighlightColor was 0xE0E0E0
        lineHighlightColor = new Color(255, 255, 215);
        lineHighlight = true;
        bracketHighlightColor = Color.black;
        bracketHighlight = true;
        eolMarkerColor = new Color(0x009999);
        eolMarkers = false;
        paintInvalid = false;
    }

    private SyntaxStyle[] getSyntaxStyles()
    {
        /* Annotated comment for hardcoded changes from the default colors
         * used in tuProlog IDE until version 1.1.2 */

        SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];
        // Token.COMMENT1 was 0x009900
        styles[Token.COMMENT1] = new SyntaxStyle(new Color(0x808080), true, false);
        // Token.COMMENT2 was 0x990033
        styles[Token.COMMENT2] = new SyntaxStyle(new Color(0x808080), true, false);
        styles[Token.KEYWORD1] = new SyntaxStyle(Color.blue, false, true);
        styles[Token.KEYWORD2] = new SyntaxStyle(Color.black, false, true);
        // Token.KEYWORD3 was Color.red
        styles[Token.KEYWORD3] = new SyntaxStyle(new Color(0xFF9900), false, true);
        // Token.LITERAL1 was 0x650099
        styles[Token.LITERAL1] = new SyntaxStyle(new Color(0xCC0000), false, false);
        // Token.LITERAL2 was 0x650099, then 0x797979, but let the two literals be highlighted in the same way
        styles[Token.LITERAL2] = new SyntaxStyle(new Color(0xCC0000), false, false);
        styles[Token.LABEL] = new SyntaxStyle(new Color(0x008000), false, false);
        styles[Token.OPERATOR] = new SyntaxStyle(Color.black, false, true);
        styles[Token.INVALID] = new SyntaxStyle(Color.red, false, true);

        return styles;
    }

}