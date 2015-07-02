package objenome.op.cas;

import objenome.op.cas.util.Token;
import objenome.op.cas.util.TokenParser;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExprTokenParser extends TokenParser {

    public Context context;
    
    public ExprTokenParser(Context context) {
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
        else if (tokenValue.equals("lonely-E")) {
            throw new ParseException("E is lonely", begin);
        }
        else if (tokenValue.equals("lonely-d")) {
            throw new ParseException("d is lonely", begin);
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
        else if (tokenValue.equals("derivativeFunc")) {
            Pattern pattern1 = Pattern.compile("^d(?:\\^(\\d+))?/d([a-zA-Z])(?:\\^(\\d+))?$");
            Matcher matcher1 = pattern1.matcher(matched);
            matcher1.find();
            String firstNum  = matcher1.group(1);
            String character = matcher1.group(2);
            String lastNum   = matcher1.group(3);
            if (firstNum == null && lastNum == null) {
                firstNum = "1";
                lastNum = "1";
            }
            if (firstNum == null || lastNum == null || Integer.parseInt(firstNum) != Integer.parseInt(lastNum)) throw new ParseException("derivative degrees don't match", begin);
            PartialParseExpr partial = new PartialParseExpr("derivativeFunc");
            partial.put("character", character.charAt(0));
            partial.put("degree", Integer.parseInt(firstNum));
            return new Token<Object>(partial, origString, begin, end);
        }
        else if (tokenValue.equals("varDerivative")) {
            Pattern pattern1 = Pattern.compile("^d(?:\\^(\\d+))?([a-zA-Z])/d([a-zA-Z])(?:\\^(\\d+))?$");
            Matcher matcher1 = pattern1.matcher(matched);
            matcher1.find();
            String firstNum  = matcher1.group(1);
            String var       = matcher1.group(2);
            String character = matcher1.group(3);
            String lastNum   = matcher1.group(4);
            if (firstNum == null && lastNum == null) {
                firstNum = "1";
                lastNum = "1";
            }
            if (firstNum == null || lastNum == null || Integer.parseInt(firstNum) != Integer.parseInt(lastNum)) throw new ParseException("derivative degrees don't match", begin);
            return new Token<Object>(Derivative.make(context.getVar(var.charAt(0)), context.getVar(character.charAt(0)), Integer.parseInt(firstNum)), origString, begin, end);
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
        else if (tokenValue.equals("equals") || tokenValue.equals("notEqual")
              || tokenValue.equals("lessThan") || tokenValue.equals("greaterThan")
              || tokenValue.equals("lessThanOrEqual") || tokenValue.equals("greaterThanOrEqual")) {
            PartialParseExpr partial = new PartialParseExpr("comparison");
            partial.put("operation", tokenValue);
            return new Token<Object>(partial, origString, begin, end);
        }
        
        return token.castValueTo(Object.class);
    }
    
}
