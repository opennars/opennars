package objenome.op.cas.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TokenListMapper {
    
    public HashMap<String,String[]> tokenMap;
    
    public boolean debug = false;
    
    public TokenListMapper(HashMap<String,String[]> tokenMap) {
        this.tokenMap = tokenMap;
    }
    
    public TokenList<Object> mapTokens(TokenList<?> tokenedW) {
        // if (debug) System.err.println("mapping tokens");
        TokenList<Object> tokened = tokenedW.castValuesTo(Object.class);

        int indexOn = 0;
        while (indexOn < tokened.size()) {
            Token<Object> tokenOn = tokened.get(indexOn);
            Object tokenValueOn = tokenOn.tokenValue;
            // if (debug) System.err.println("on token: "+tokenOn);
            
            if (tokenValueOn instanceof String && tokenMap.containsKey(tokenValueOn)) {
                List<Token<Object>> subList = tokened.subList(indexOn, indexOn + 1);
                String[] replacement = tokenMap.get(tokenValueOn);
                Token<Object>[] replacementTokens = new Token[replacement.length];
                for (int i = 0; i < replacement.length; i++) {
                    replacementTokens[i] = new Token<>(replacement[i], tokenOn, tokenOn);
                }
                
                subList.clear();
                subList.addAll(Arrays.asList(replacementTokens));
                
                indexOn--;
            }
            
            indexOn++;
        }
        
        if (debug) System.err.println("mapped to: " + tokened);
        // if (debug) System.err.println("done mapping tokens");
        return tokened;
    }
    
    public TokenListMapper withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
    
}
