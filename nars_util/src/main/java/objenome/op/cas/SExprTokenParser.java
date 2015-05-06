package objenome.op.cas;

import java.text.ParseException;

import objenome.op.cas.util.Token;
import objenome.op.cas.util.TokenParser;

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
        
        if (tokenValue.equals("piWord") || tokenValue.equals("pi")) {
            return new Token<Object>(new Pi(), origString, begin, end);
        }
        else if (tokenValue.equals("e")) {
            return new Token<Object>(new E(), origString, begin, end);
        }
        else if (tokenValue.equals("i")) {
            // if (debug) System.err.println("i am not yet supported");
            throw new ParseException("i am not yet supported", begin);
        }
        else if (tokenValue.equals("var")) {
            return new Token<Object>(context.getVar(matched.charAt(0)), origString, begin, end);
        }
        else if (tokenValue.equals("number")) {
            return new Token<Object>(Num.make(Double.parseDouble(matched)), origString, begin, end);
        }
        else if (tokenValue.equals("undef")) {
            return new Token<Object>(new Undef(), origString, begin, end);
        }
        else if (tokenValue.equals("true")) {
            return new Token<Object>(Expr.yep(), origString, begin, end);
        }
        else if (tokenValue.equals("false")) {
            return new Token<Object>(Expr.nope(), origString, begin, end);
        }
        
        return token.castValueTo(Object.class);
    }
    
}
