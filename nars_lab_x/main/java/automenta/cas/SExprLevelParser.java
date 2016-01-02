//package langdon.math;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.text.ParseException;
//import java.lang.reflect.Method;
//
//import langdon.util.*;
//
//public class SExprLevelParser implements LevelParser {
//
//    public Token[] spaceTokenArr = {new Token<Object>("space")};
//
//    public HashMap<String,Integer> parseDir = new HashMap<String,Integer>();
//
//    public Context context;
//
//    public boolean debug = false;
//
//    public SExprLevelParser(Context context) {
//        this.context = context;
//    }
//
//    public Token<Object> parseLevel(Token[] delims, TokenList<Object> tokened) throws ParseException {
//
//        if (debug) System.err.println("parsing     " + tokened);
//
//        if (tokened.size() == 0) throw new ParseException("token(s) expected, but not found (check your syntax)", tokened.fromStrBegin);
//        Token<Object> firstToken = tokened.get(0).castValueTo(Object.class);
//        if (tokened.size() == 1 && firstToken.tokenValue instanceof Expr) {
//            return firstToken;
//        }
//
//        if ((Token.getValues(tokened)).contains("space")) {
//            if (debug) System.err.println("splitting on {space}");
//
//            ArrayList<Token<?>> splitOnAL = new ArrayList();
//
//            ArrayList<TokenList<Object>> splitted = TokenList.toArrTokenList(ArrayLists.split( tokened, spaceTokenArr, 0, splitOnAL));
//            if (splitted.get(0).fromStrBegin == null) splitted.get(0).fromStrBegin = tokened.fromStrBegin;
//            if (splitted.get(splitted.size() - 1).fromStrBegin == null) {
//                splitted.get(splitted.size() - 1).fromStrBegin = tokened.get(tokened.size() - 1).fromStrEnd;
//            }
//
//            if (debug) System.err.println("splitted: " + splitted);
//            Token[] opTokenPair = {spaceTokenArr[0], spaceTokenArr[0]};
//
//            String func = splitted.get(0).get(0).fromStrPart();
//            ArrayList<Token<Expr>> parsed = new ArrayList<Token<Expr>>();
//            for (int j = 1; j < splitted.size(); j++) {
//                TokenList<Object> toParse = splitted.get(j);
//                parsed.add(parseLevel(opTokenPair, toParse).castValueTo(Expr.class));
//            }
//
//            try {
//                Class funcClass = Class.forName("langdon.math." + func);
//                if (Operation.class.isAssignableFrom(funcClass)) {
//                    Method funcMake = funcClass.getMethod("make", ArrayList.class);
//                    // System.err.println(funcMake);
//                    return new Token<Object>(funcMake.invoke(null, Token.getValues(parsed)), splitted.get(0).get(0), parsed.get(parsed.size() - 1));
//                }
//            } catch (Exception e) {
//                // System.err.println(e);
//            }
//
//            if (debug) System.err.println(func + " operation probably not supported");
//            throw new ParseException(func + " operation probably not supported", splitted.get(0).fromStrBegin);
//        }
//
//        throw new ParseException("no operation found", tokened.fromStrBegin);
//    }
//
//    public SExprLevelParser withDebug(boolean debug) {
//        this.debug = debug;
//        return this;
//    }
//
//}
