package objenome.op.cas;

import objenome.op.cas.util.Token;
import objenome.op.cas.util.TokenParser;

import java.text.ParseException;

public class SExprTokenParser extends TokenParser {

    public Context context;
    
    public SExprTokenParser(Context context) {
        this.context = context;
    }
    
    public Token<Object> parseToken(Token<?> token) throws ParseException {
        Object tokenValue = token.tokenValue;
        String origString = token.fromStr;
        int begin = token.fromStrBegin;
        int end = token.fromStrEnd;
        String matched = origString.substring(begin, end);
        
        if ("piWord".equals(tokenValue) || "pi".equals(tokenValue)) {
            return new Token<>(new Pi(), origString, begin, end);
        }
        //noinspection IfStatementWithTooManyBranches
        if ("e".equals(tokenValue)) {
            return new Token<>(new E(), origString, begin, end);
        }
        if ("i".equals(tokenValue)) {
            // if (debug) System.err.println("i am not yet supported");
            throw new ParseException("i am not yet supported", begin);
        }
        if ("var".equals(tokenValue)) {
            return new Token<>(context.getVar(matched.charAt(0)), origString, begin, end);
        }
        if ("number".equals(tokenValue)) {
            return new Token<>(Num.make(Double.parseDouble(matched)), origString, begin, end);
        }
        if ("undef".equals(tokenValue)) {
            return new Token<>(new Undef(), origString, begin, end);
        }
        if ("true".equals(tokenValue)) {
            return new Token<>(Expr.yep(), origString, begin, end);
        }
        if ("false".equals(tokenValue)) {
            return new Token<>(Expr.nope(), origString, begin, end);
        }

        return token.castValueTo(Object.class);
    }
    
}
