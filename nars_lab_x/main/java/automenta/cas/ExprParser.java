//package langdon.math;
//
//import langdon.util.*;
//
//import java.util.HashMap;
//import java.text.ParseException;
//
//public abstract class ExprParser {
//
//    public static void main(String[] args) {
//        try {
//            Expr expr = parseExpr(args[0]);
//            System.out.println(expr);
//        } catch (ParseException e) {
//            System.err.println(generateParseMesg(args[0], e));
//        }
//    }
//
//    public static String[][] tokens
//            = {{"\\s+","space"},
//               {"d(\\^\\d+)?[a-zA-Z]/d[a-zA-Z](\\^\\d+)?","varDerivative"},
//               {"d(\\^\\d+)?/d[a-zA-Z](\\^\\d+)?","derivativeFunc"},
//               {"undef", "undef"},
//               {"true", "true"},
//               {"false", "false"},
//               {"sqrt", "sqrt"},
//               {"not", "not"},
//               {"and", "and"},
//               {"sin", "sin"}, // (?![a-zA-Z])
//               {"cos", "cos"},
//               {"tan", "tan"},
//               {"cot", "cot"},
//               {"sec", "sec"},
//               {"csc", "csc"},
//               {"log", "log"},
//               {"if", "if"},
//               {"or", "or"},
//               {"ln", "ln"},
//               {"pi", "piWord"},
//               {"e", "e"},
//               {"E", "lonely-E"},
//               {"d", "lonely-d"},
//               {"i", "i"},
//               {"[a-zA-Z]", "var"},
//               {"(\\d+(\\.\\d+)?|\\.\\d+)(E(\\+|-)?\\d+)?", "number"},
//               {"\\+", "plus"},
//               {"-", "minusHyph"},
//               {"\\*", "timesAst"},
//               {"/", "divisionSlash"},
//               {"\\(", "leftParen"},
//               {"\\)", "rightParen"},
//               {"\\[", "leftBracket"},
//               {"\\]", "rightBracket"},
//               {"\\^", "exponentCaret"},
//               {"<=", "lessThanOrEqual"},
//               {">=", "greaterThanOrEqual"},
//               {"<", "lessThan"},
//               {">", "greaterThan"},
//               {"=", "equals"},
//               {"!=", "notEqual"},
//               {",", "comma"},
//               {"~", "squiggly"},
//               {"&&","2amp"},
//               {"||","2bar"}};
//
//    public static String[][] sExprTokens
//            = {{"\\s+","space"},
//               {"undef", "undef"},
//               {"true", "true"},
//               {"false", "false"},
//               {"if", "if"},
//               {"pi", "piWord"},
//               {"e", "e"},
//               {"i", "i"},
//               {"[a-zA-Z][a-zA-Z]+", "func"},
//               {"[a-zA-Z]", "var"}, // (?![a-zA-Z])
//               {"(\\d+(\\.\\d+)?|\\.\\d+)(E(\\+|-)?\\d+)?", "number"},
//               {"\\(", "leftParen"},
//               {"\\)", "rightParen"}};
//
//    public static HashMap<String,String[]> tokenMap = new HashMap<String,String[]>();
//    static {
//        tokenMap.put("space", new String[]{});
//        tokenMap.put("minusHyph", new String[]{"minus"});
//        tokenMap.put("timesAst", new String[]{"times"});
//        tokenMap.put("divisionSlash", new String[]{"division"});
//        tokenMap.put("exponentCaret", new String[]{"exponent"});
//        tokenMap.put("piWord", new String[]{"pi"});
//        tokenMap.put("squiggly", new String[]{"not"});
//        tokenMap.put("2amp", new String[]{"and"});
//        tokenMap.put("2bar", new String[]{"or"});
//    }
//
//    public static Token[][] levelDelims = {{new Token<Object>("leftParen"), new Token<Object>("rightParen")},
//                                           {new Token<Object>("leftBracket"), new Token<Object>("rightBracket")}};
//
//    public static Token[][] sExprLevelDelims = {{new Token<Object>("leftParen"), new Token<Object>("rightParen")}};
//
//    private static String noTokenMsg = "invalid token";
//
//    private static boolean debug = true;
//
//    public static Expr parseExpr(String string) throws ParseException {
//        return parseExpr(string, new Context());
//    }
//
//    public static Expr parseExpr(String string, Context context) throws ParseException {
//        return parseExpr(new TokenListMapper(tokenMap).withDebug(debug).mapTokens(
//                new ExprTokenParser(context).withDebug(debug).parseTokenList(
//                new Tokenizer(tokens).withDebug(debug).tokenize(string))), context);
//    }
//
////     public static Expr parseExpr(ArrayList<Object> tokened) throws ParseException {
////         return parseExpr(tokened, new Context());
////     }
//
//    public static Expr parseExpr(TokenList<Object> tokened, Context context) throws ParseException {
//        return parse(tokened, context).tokenValue;
//    }
//
//    public static Token<Expr> parse(TokenList<Object> tokened, Context context) throws ParseException {
//        return new LevelsParser(levelDelims, new ExprLevelParser(context).withDebug(debug)).withDebug(debug)
//                .withAfterPopHandler(new ParenFunctioning().withDebug(debug)).parseLevels(tokened)
//                .castValueTo(Expr.class);
//    }
//
//    public static String generateParseMesg(String input, ParseException e) {
//        // if (!e.getMessage().equals(noTokenMsg)) return null;
//
//        String string = input+"\n";
//        for (int i = e.getErrorOffset(); i>0; i--) {
//            string = string.concat(" ");
//        }
//        string = string.concat("^");
//
//        return string;
//    }
//
//    public static void noDebug() {
//        debug = false;
//    }
//
//    public static Expr parseSExprExpr(String string) throws ParseException {
//        return parseSExprExpr(string, new Context());
//    }
//
//    public static Expr parseSExprExpr(String string, Context context) throws ParseException {
//        return parseSExprExpr(new SExprTokenParser(context).withDebug(debug).parseTokenList(
//                new Tokenizer(sExprTokens).withDebug(debug).tokenize(string)), context);
//    }
//
//    public static Expr parseSExprExpr(TokenList<?> tokened, Context context) throws ParseException {
//        return parseSExpr(tokened, context).tokenValue;
//    }
//
//    public static Token<Expr> parseSExpr(TokenList<?> tokened, Context context) throws ParseException {
//        return new LevelsParser(sExprLevelDelims, new SExprLevelParser(context).withDebug(debug)).withDebug(debug)
//                .parseLevels(tokened)
//                .castValueTo(Expr.class);
//    }
//
//}
