package objenome.op.cas.util;

import java.text.ParseException;

public interface AfterLevelPopHandler {
    
    public TokenList<Object> handleAfterPop(Token[] delims, TokenList<?> currentLevel, LevelParser levelParser) throws ParseException;
    
}
