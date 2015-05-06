package objenome.op.cas.util;

import java.text.ParseException;

public abstract class TokenParser {
    
    public boolean debug = false;
    
    public abstract Token<Object> parseToken(Token<?> token) throws ParseException;
    
    public TokenList<Object> parseTokenList(TokenList<?> tokens) throws ParseException {
        TokenList<Object> newTokens = new TokenList(tokens.fromStrBegin);
        for (Token<?> token : tokens) {
            Token<Object> parsedToken = parseToken(token);
            newTokens.add(parsedToken);
        }
        
        if (debug) System.err.println("parsed to: " + newTokens);
        return newTokens;
    }
    
    public TokenParser withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
    
}
