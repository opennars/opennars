package objenome.op.cas.util;

import java.text.ParseException;

public interface AfterLevelPopHandler {
    
    TokenList<Object> handleAfterPop(Token[] delims, TokenList<?> currentLevel, LevelParser levelParser) throws ParseException;
    
}
