//package langdon.math;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.lang.reflect.Array;
//import java.util.Arrays;
//import java.text.ParseException;
//
//import langdon.util.*;
//
//public class ExprLevelParser implements LevelParser {
//
//    public Object[][] breakOrder = {{"comma"},
//                                    {"if"},
//                                    {"or"},
//                                    {"and"},
//                                    {"not"},
//                                    {"comparisonDummy", new PartialParseExpr("comparison")},
//                                    {"plus"},
//                                    {"sin", "cos", "tan", "cot", "sec", "csc", "log", "ln", new PartialParseExpr("derivativeFunc"), "sqrt"},
//                                    {"times", "division"},
//                                    {"minus"},
//                                    {"exponent"}};
//
//    public final Token<?>[][] tokenBreakOrder = new Token[breakOrder.length][];
//
//    public HashMap<String,Integer> parseDir = new HashMap<String,Integer>();
//
//    public Token[] subscriptTokenPair = {new Token("subscriptBeg"), new Token("subscriptEnd")};
//
//    public Context context;
//
//    public boolean debug = false;
//
//    public ExprLevelParser(Context context) {
//        this.context = context;
//
//        for (int i = 0; i < breakOrder.length; i++) {
//            Token<Object>[] thisLevel = new Token[breakOrder[i].length];
//            for (int j = 0; j < breakOrder[i].length; j++) {
//                thisLevel[j] = new Token<Object>(breakOrder[i][j]);
//            }
//            tokenBreakOrder[i] = thisLevel;
//        }
//
//        parseDir.put("plus", 0);
//        parseDir.put("sin", -1); // right-associative
//        parseDir.put("cos", -1);
//        parseDir.put("tan", -1);
//        parseDir.put("cot", -1);
//        parseDir.put("sec", -1);
//        parseDir.put("csc", -1);
//        parseDir.put("log", -1);
//        parseDir.put("ln", -1);
//        parseDir.put("derivativeFunc", -1);
//        parseDir.put("sqrt", -1);
//        parseDir.put("times", 1);
//        parseDir.put("division", 1); // left-associative
//        parseDir.put("minus", -1);
//        parseDir.put("exponent", -1);
//        parseDir.put("if", 1);
//        parseDir.put("comparisonDummy", 2); // chaining or whatever
//        parseDir.put("comma", 0);
//        parseDir.put("not", -1);
//        parseDir.put("or", 0);
//        parseDir.put("and", 0);
//    }
//
//    public Token<Object> parseLevel(Token[] delims, TokenList<Object> tokened) throws ParseException {
//        if (delims[0].equals(new Token("leftBracket")) && delims[1].equals(new Token("rightBracket"))) {
//            Token<Object> subscriptT = parseLevel(subscriptTokenPair, tokened);
//            return new Token<Object>(new Subscript(subscriptT.castValueTo(Expr.class).tokenValue), subscriptT, subscriptT);
//        }
//
//        if (debug) System.err.println("parsing     " + tokened);
//
//        if (tokened.size() == 0) throw new ParseException("token(s) expected, but not found (check your syntax)", tokened.fromStrBegin);
//        Token<Object> firstToken = tokened.get(0).castValueTo(Object.class);
//        if (tokened.size() == 1 && firstToken.tokenValue instanceof Expr) {
//            return firstToken;
//        }
//
//        tokened = alterOperations(tokened);
//
//        for (int i = 0; i < Array.getLength(breakOrder); i++) {
//            if (ArrayLists.containsIn(Token.getValues(tokened), breakOrder[i])) {
//                if (debug) System.err.println("splitting on " + Arrays.toString(tokenBreakOrder[i]));
//
//                int tokenParseDir = parseDir.get(breakOrder[i][0]);
//
//                ArrayList<Token> splitOnAL = new ArrayList();
//
//                ArrayList<TokenList<Object>> splitted = TokenList.toArrTokenList(ArrayLists.split(tokened, tokenBreakOrder[i], tokenParseDir, splitOnAL));
//
//                if (splitted.get(0).fromStrBegin == null) splitted.get(0).fromStrBegin = tokened.fromStrBegin;
//                if (splitted.get(splitted.size() - 1).fromStrBegin == null) {
//                    splitted.get(splitted.size() - 1).fromStrBegin = tokened.get(tokened.size() - 1).fromStrEnd;
//                }
//
//                Token<Object> splitOnToken = splitOnAL.get(0);
//                Object splitOn = splitOnToken.tokenValue;
//                if (debug) System.err.println("splitted: " + splitted);
//                Token[] opTokenPair = {splitOnToken, splitOnToken};
//
//                if (splitOn.equals("plus")) {
//                    ArrayList<Token<Expr>> parsed = new ArrayList<Token<Expr>>();
//                    for (TokenList<Object> toParse : splitted) {
//                        parsed.add(parseLevel(opTokenPair, toParse).castValueTo(Expr.class));
//                    }
//                    // if (debug) System.err.println("sum: " + Sum.make(parsed));
//                    return new Token<Object>(Sum.make(Token.getValues(parsed)), parsed.get(0), parsed.get(parsed.size() - 1));
//                }
//                else if (splitOn.equals("minus")) {
//                    Token<Expr> minused = parseLevel(opTokenPair, splitted.remove(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Product.make(Num.make(-1d), minused.tokenValue), splitOnToken, minused));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("times")) {
//                    Token<Expr> firstFactor = parseLevel(opTokenPair, splitted.get(0)).castValueTo(Expr.class);
//                    Token<Expr> secondFactor = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    return new Token<Object>(Product.make(firstFactor.tokenValue, secondFactor.tokenValue), firstFactor, secondFactor);
//                }
//                else if (splitOn.equals("exponent")) {
//                    Token<Expr> base = parseLevel(opTokenPair, splitted.get(0)).castValueTo(Expr.class);
//                    Token<Expr> exponent = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    return new Token<Object>(Exponent.make(base.tokenValue, exponent.tokenValue), base, exponent);
//                }
//                else if (splitOn.equals("division")) {
//                    Token<Expr> dividend = parseLevel(opTokenPair, splitted.get(0)).castValueTo(Expr.class);
//                    Token<Expr> divisor = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    return new Token<Object>(Division.make(dividend.tokenValue, divisor.tokenValue), dividend, divisor);
//                }
//                else if (splitOn.equals("log")) {
//                    Expr base = (splitted.get(1).get(0).tokenValue instanceof Subscript) ? ((Subscript) splitted.get(1).remove(0).tokenValue).getExpr() : Num.make(10d);
//                    Token<Expr> logOf = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Logarithm.make(base, logOf.tokenValue), splitOnToken, logOf));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("ln")) {
//                    Token<Expr> logOf = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Logarithm.make(new E(), logOf.tokenValue), splitOnToken, logOf));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("sin")) {
//                    Token<Expr> sinOf = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Sin.make(sinOf.tokenValue), splitOnToken, sinOf));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("cos")) {
//                    Token<Expr> cosOf = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Cos.make(cosOf.tokenValue), splitOnToken, cosOf));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("tan")) {
//                    Token<Expr> tanOf = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Division.make(Sin.make(tanOf.tokenValue), Cos.make(tanOf.tokenValue)), splitOnToken, tanOf));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("cot")) {
//                    Token<Expr> cotOf = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Division.make(Cos.make(cotOf.tokenValue), Sin.make(cotOf.tokenValue)), splitOnToken, cotOf));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("sec")) {
//                    Token<Expr> secOf = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Division.make(Num.make(1), Cos.make(secOf.tokenValue)), splitOnToken, secOf));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("csc")) {
//                    Token<Expr> cscOf = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Division.make(Num.make(1), Sin.make(cscOf.tokenValue)), splitOnToken, cscOf));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals(new PartialParseExpr("derivativeFunc"))) {
//                    // if (debug) System.err.println(((PartialParseExpr) splitOn).hash);
//                    Token<Expr> derivOf = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Derivative.make(derivOf.tokenValue, context.getVar((Character) ((PartialParseExpr) splitOn).get("character")),
//                            (Integer) ((PartialParseExpr) splitOn).get("degree")), splitOnToken, derivOf));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("sqrt")) {
//                    Token<Expr> sqrtOf = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Exponent.make(sqrtOf.tokenValue, Num.make(0.5)), splitOnToken, sqrtOf));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("if")) {
//                    Token<Expr> then = parseLevel(opTokenPair, splitted.get(0)).castValueTo(Expr.class);
//                    Token<Expr> ifIs = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    return new Token<Object>(Conditional.make(ifIs.tokenValue, then.tokenValue), then, ifIs);
//                }
//                else if (splitOn.equals(new PartialParseExpr("comparison"))) {
//                    ArrayList<Token<Expr>> parsed = parseArrayList(splitted, opTokenPair);
//                    ArrayList<Expr> ands = new ArrayList<Expr>();
//
//                    for (int j = 0; j < parsed.size() - 1; j++) {
//                        Expr expr1 = parsed.get(j).tokenValue;
//                        Expr expr2 = parsed.get(j + 1).tokenValue;
//                        Object operation = ((PartialParseExpr) splitOnAL.get(j).tokenValue).get("operation");
//
//                        if (operation.equals("equals")) {
//                            ands.add(Equals.make(expr1, expr2));
//                        } else if (operation.equals("notEqual")) {
//                            ands.add(NotEqual.make(expr1, expr2));
//                        } else if (operation.equals("lessThan")) {
//                            ands.add(LessThan.make(expr1, expr2));
//                        } else if (operation.equals("greaterThan")) {
//                            ands.add(GreaterThan.make(expr1, expr2));
//                        } else if (operation.equals("lessThanOrEqual")) {
//                            ands.add(LessThanOrEqual.make(expr1, expr2));
//                        } else if (operation.equals("greaterThanOrEqual")) {
//                            ands.add(GreaterThanOrEqual.make(expr1, expr2));
//                        } else {
//                            if (debug) System.err.println(operation + " operation not yet supported");
//                            throw new ParseException(operation + " operation not yet supported", splitOnAL.get(j).fromStrBegin);
//                        }
//                    }
//                    return new Token<Object>(And.make(ands), parsed.get(0), parsed.get(parsed.size() - 1));
//                }
//                else if (splitOn.equals("equals")) {
//                    ArrayList<Token<Expr>> parsed = new ArrayList<Token<Expr>>();
//                    for (TokenList<Object> toParse : splitted) {
//                        parsed.add(parseLevel(opTokenPair, toParse).castValueTo(Expr.class));
//                    }
//                    return new Token<Object>(Equals.make(Token.getValues(parsed)), parsed.get(0), parsed.get(parsed.size() - 1));
//                }
//                else if (splitOn.equals("comma")) {
//                    ArrayList<Token<Expr>> parsed = new ArrayList<Token<Expr>>();
//                    for (TokenList<Object> toParse : splitted) {
//                        parsed.add(parseLevel(opTokenPair, toParse).castValueTo(Expr.class));
//                    }
//                    return new Token<Object>(Piecewise.make(Token.getValues(parsed)), parsed.get(0), parsed.get(parsed.size() - 1));
//                }
//                else if (splitOn.equals("not")) {
//                    Token<Expr> notThis = parseLevel(opTokenPair, splitted.get(1)).castValueTo(Expr.class);
//                    splitted.get(0).add(new Token<Object>(Not.make(notThis.tokenValue), splitOnToken, notThis));
//                    return parseLevel(opTokenPair, splitted.get(0));
//                }
//                else if (splitOn.equals("or")) {
//                    ArrayList<Token<Expr>> parsed = new ArrayList<Token<Expr>>();
//                    for (TokenList<Object> toParse : splitted) {
//                        parsed.add(parseLevel(opTokenPair, toParse).castValueTo(Expr.class));
//                    }
//                    return new Token<Object>(Or.make(Token.getValues(parsed)), parsed.get(0), parsed.get(parsed.size() - 1));
//                }
//                else if (splitOn.equals("notEqual")) {
//                    ArrayList<Token<Expr>> parsed = new ArrayList<Token<Expr>>();
//                    for (TokenList<Object> toParse : splitted) {
//                        parsed.add(parseLevel(opTokenPair, toParse).castValueTo(Expr.class));
//                    }
//                    return new Token<Object>(NotEqual.make(Token.getValues(parsed)), parsed.get(0), parsed.get(parsed.size() - 1));
//                }
//                else if (splitOn.equals("and")) {
//                    ArrayList<Token<Expr>> parsed = new ArrayList<Token<Expr>>();
//                    for (TokenList<Object> toParse : splitted) {
//                        parsed.add(parseLevel(opTokenPair, toParse).castValueTo(Expr.class));
//                    }
//                    return new Token<Object>(And.make(Token.getValues(parsed)), parsed.get(0), parsed.get(parsed.size() - 1));
//                }
//                else if (splitOn.equals("greaterThan")) {
//                    ArrayList<Token<Expr>> parsed = parseArrayList(splitted, opTokenPair);
//                    return new Token<Object>(GreaterThan.make(Token.getValues(parsed)), parsed.get(0), parsed.get(parsed.size() - 1));
//                }
//                else if (splitOn.equals("lessThanOrEqual")) {
//                    ArrayList<Token<Expr>> parsed = parseArrayList(splitted, opTokenPair);
//                    return new Token<Object>(LessThanOrEqual.make(Token.getValues(parsed)), parsed.get(0), parsed.get(parsed.size() - 1));
//                }
//                else if (splitOn.equals("greaterThanOrEqual")) {
//                    ArrayList<Token<Expr>> parsed = parseArrayList(splitted, opTokenPair);
//                    return new Token<Object>(GreaterThanOrEqual.make(Token.getValues(parsed)), parsed.get(0), parsed.get(parsed.size() - 1));
//                }
//                else {
//                    if (debug) System.err.println(splitOn + " operation not yet supported");
//                    throw new ParseException(splitOn + " operation not yet supported", splitOnToken.fromStrBegin);
//                }
//            }
//        }
//
//        throw new ParseException("no operation found", tokened.fromStrBegin);
//    }
//
//    public TokenList<Object> alterOperations(TokenList<?> tokenedW) {
//        TokenList<Object> tokened = (TokenList<Object>) tokenedW.clone();
//
//        for (int i = 1; i < tokened.size(); i++) {
//            if (tokened.get(i - 1).tokenValue instanceof Expr && tokened.get(i).tokenValue instanceof Expr) {
//                tokened.add(i, new Token<Object>("times", tokened.get(i).fromStr, tokened.get(i).fromStrBegin, tokened.get(i).fromStrBegin));
//            }
//            else if (tokened.get(i - 1).tokenValue instanceof Expr && tokened.get(i).valueEquals("minus")) {
//                tokened.add(i, new Token<Object>("plus", tokened.get(i).fromStr, tokened.get(i).fromStrBegin, tokened.get(i).fromStrBegin));
//            }
//        }
//
//        if (debug) System.err.println("altered to: " + tokened);
//        return tokened;
//    }
//
//    public ArrayList<Token<Expr>> parseArrayList(ArrayList<TokenList<Object>> splitted, Token[] opTokenPair) throws ParseException {
//        ArrayList<Token<Expr>> parsed = new ArrayList<Token<Expr>>();
//        for (TokenList<Object> toParse : splitted) {
//            parsed.add(parseLevel(opTokenPair, toParse).castValueTo(Expr.class));
//        }
//        return parsed;
//    }
//
//    public ExprLevelParser withDebug(boolean debug) {
//        this.debug = debug;
//        return this;
//    }
//
//}
