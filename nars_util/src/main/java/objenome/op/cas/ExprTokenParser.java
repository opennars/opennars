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
        
        if ("piWord".equals(tokenValue) || "pi".equals(tokenValue)) {
            return new Token<>(new Pi(), origString, begin, end);
        }
        //noinspection IfStatementWithTooManyBranches
        if ("e".equals(tokenValue)) {
            return new Token<>(new E(), origString, begin, end);
        }
        if ("lonely-E".equals(tokenValue)) {
            throw new ParseException("E is lonely", begin);
        }
        if ("lonely-d".equals(tokenValue)) {
            throw new ParseException("d is lonely", begin);
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
        if ("derivativeFunc".equals(tokenValue)) {
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
            return new Token<>(partial, origString, begin, end);
        }
        if ("varDerivative".equals(tokenValue)) {
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
            return new Token<>(Derivative.make(context.getVar(var.charAt(0)), context.getVar(character.charAt(0)), Integer.parseInt(firstNum)), origString, begin, end);
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
        if ("equals".equals(tokenValue) || "notEqual".equals(tokenValue)
              || "lessThan".equals(tokenValue) || "greaterThan".equals(tokenValue)
              || "lessThanOrEqual".equals(tokenValue) || "greaterThanOrEqual".equals(tokenValue)) {
            PartialParseExpr partial = new PartialParseExpr("comparison");
            partial.put("operation", tokenValue);
            return new Token<>(partial, origString, begin, end);
        }

        return token.castValueTo(Object.class);
    }
    
}
