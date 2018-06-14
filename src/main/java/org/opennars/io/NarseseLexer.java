package org.opennars.io;

import java.util.regex.Pattern;

public class NarseseLexer extends Lexer {
    static public final int INTRO = 1;
    static public final int OUTRO = 2;

    static public final int BRACKETOPEN = 3;
    static public final int BRACKETCLOSE = 4;

    static public final int POUNDKEY = 5;
    static public final int SIMILARITY = 6;
    static public final int INHERITANCE = 7;

    @Override
    protected Token createToken(int ruleIndex, String matchedString) {
        Token token = new Token();

        if (ruleIndex == 1) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = INTRO;
        }
        else if (ruleIndex == 2) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = OUTRO;
        }
        else if (ruleIndex == 3) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = BRACKETOPEN;
        }
        else if (ruleIndex == 4) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = BRACKETCLOSE;
        }
        else if (ruleIndex == 5) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = POUNDKEY;
        }
        else if (ruleIndex == 6) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = SIMILARITY;
        }
        else if (ruleIndex == 7) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = INHERITANCE;
        }
        else if (ruleIndex == 8) {
            token.type = Token.EnumType.IDENTIFIER;
            token.contentString = matchedString;
        }

        /*
        if( ruleIndex == 000 ) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = EnumOperationType.BRACEOPEN;
        }
        else if( ruleIndex == 001 ) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = EnumOperationType.BRACECLOSE;
        }

        else if( ruleIndex == 009 ) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = KEY;
            token.contentString = matchedString;
        }
        else if( ruleIndex == 0010 ) {
            token.type = Token.EnumType.OPERATION;
            token.contentOperation = HALFH;
        }
         */

        return token;
    }

    @Override
    protected void fillRules() {
        tokenRules.add(new Rule(Pattern.compile(("^([ \n\r]+)"))));

        tokenRules.add(new Rule(Pattern.compile(("^(<)"))));
        tokenRules.add(new Rule(Pattern.compile(("^(>)"))));

        tokenRules.add(new Rule(Pattern.compile(("^(\\[)"))));
        tokenRules.add(new Rule(Pattern.compile(("^(\\])"))));

        tokenRules.add(new Rule(Pattern.compile(("^(#)"))));
        tokenRules.add(new Rule(Pattern.compile(("^(<->)"))));
        tokenRules.add(new Rule(Pattern.compile(("^(-->)"))));
        tokenRules.add(new Rule(Pattern.compile(("^([a-zA-Z][0-9A-Za-z]*)"))));

        //tokenRules.add(new Rule(Pattern.compile(("^(\\()"))));
        //tokenRules.add(new Rule(Pattern.compile(("^(\\))"))));
        //tokenRules.add(new Rule(Pattern.compile(("^(:[a-zA-Z/\\-\\?!=]+)"))));
        //tokenRules.add(new Rule(Pattern.compile(("^(\\|-)")))); // HALF-H
    }
}
