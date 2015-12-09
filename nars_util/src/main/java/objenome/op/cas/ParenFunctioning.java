package objenome.op.cas;

import objenome.op.cas.util.AfterLevelPopHandler;
import objenome.op.cas.util.LevelParser;
import objenome.op.cas.util.Token;
import objenome.op.cas.util.TokenList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ParenFunctioning implements AfterLevelPopHandler {
    
    public ArrayList<Object> functions = new ArrayList<>();
    
    public Token[] doItTokenPair = {new Token<Object>("leftParen"),
                                 new Token<Object>("rightParen")};
    
    public Token[] functioningTokenPair = {new Token<Object>("parenFuncBeg"),
                                           new Token<Object>("parenFuncEnd")};
    
    public boolean debug = false;
    
    public ParenFunctioning() {
        functions.add("sin");
        functions.add("cos");
        functions.add("tan");
        functions.add("cot");
        functions.add("sec");
        functions.add("csc");
        functions.add("log");
        functions.add("ln");
        functions.add("derivativeFunc");
        functions.add("sqrt");
        functions.add("not");
    }
    
    public TokenList<Object> handleAfterPop(Token[] delims, TokenList<?> currentLevelW, LevelParser levelParser) throws ParseException {
        TokenList<Object> currentLevel = currentLevelW.castValuesTo(Object.class);
        if (!delims[0].equals(doItTokenPair[0])) return currentLevel;
        if (!delims[1].equals(doItTokenPair[1])) return currentLevel;
        
        for (int itr = currentLevel.size() - 2; itr >= 0; itr--) {
            if (!(currentLevel.get(itr).tokenValue instanceof Subscript)) {
                if (functions.contains(currentLevel.get(itr).tokenValue)) {
                    if (debug) System.err.println("currentLevel before: " + currentLevel);
                    List<Token<Object>> mkFuncList = currentLevel.subList(itr, currentLevel.size());
                    // System.err.println(mkFuncList);
                    Token<Object> parsedFunc = levelParser.parseLevel(functioningTokenPair, new TokenList<>(mkFuncList));
                    mkFuncList.clear();
                    mkFuncList.add(parsedFunc);
                    // System.err.println(mkFuncList);
                    if (debug) System.err.println("currentLevel after:  " + currentLevel);
                }
                break;
            }
        }
        return currentLevel;
    }
    
    public ParenFunctioning withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
    
}
