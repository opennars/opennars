package org.opennars.io;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Lexer {
    protected List<Rule> tokenRules = new ArrayList<>();
    // token rule #0 is ignored, because it contains the pattern for spaces

    private String remainingSource;

    // position in Source File
    private String actualFilename = "<stdin>";
    private int actualLine = 1;
    private int actualColumn = 0;

    protected Token resultToken;
    protected int index;

    public EnumLexerCode nextToken() {
        for(;;) {
            EnumLexerCode lexerCode = nextTokenInternal();
            if (lexerCode != EnumLexerCode.OK || resultToken.type == Token.EnumType.EOF) {
                return lexerCode;
            }

            if (index == 0) {
                continue;
            }

            return lexerCode;
        }
    }

    protected EnumLexerCode nextTokenInternal() {
        final boolean endReached = remainingSource.length() == 0;
        if (endReached) {
            resultToken = new Token();
            resultToken.type = Token.EnumType.EOF;
            return EnumLexerCode.OK;
        }

        int iindex = 0;
        for (Rule iRule : tokenRules) {
            final Matcher iMatcher = iRule.regularExpression.matcher(remainingSource);
            if (iMatcher.find()) {
                final String matchedString = iMatcher.group();
                remainingSource = remainingSource.substring(matchedString.length());

                resultToken = createToken(iindex, matchedString);
                return EnumLexerCode.OK;
            }

            iindex++;
        }

        return EnumLexerCode.INVALID;
    }

    public Lexer() {
        fillRules();
    }

    abstract protected Token createToken(int ruleIndex, String matchedString);

    abstract protected void fillRules();

    static class Rule {
        public Pattern regularExpression; // regular expression its matched with

        public Rule(Pattern regularExpression) {
            this.regularExpression = regularExpression;
        }
    }

    public enum EnumLexerCode {
        OK,
        INVALID
    }
}
