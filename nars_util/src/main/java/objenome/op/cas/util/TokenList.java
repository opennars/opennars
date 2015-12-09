package objenome.op.cas.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class TokenList<T> extends ArrayList<Token<T>> {
    
    public Integer fromStrBegin = null;
    
    public TokenList(int fromStrBegin) {
        this.fromStrBegin = fromStrBegin;
    }
    
    public TokenList(Collection<Token<T>> tokens, int fromStrBegin) {
        super(tokens);
        this.fromStrBegin = fromStrBegin;
    }
    
    public TokenList(Collection<Token<T>> tokens) {
        super(tokens);
        if (size() > 0) fromStrBegin = get(0).fromStrBegin;
    }
    
    public static <T> ArrayList<TokenList<T>> toArrTokenList(Collection<ArrayList<Token<T>>> tokenArrArr) {
        ArrayList<TokenList<T>> tokenListList = tokenArrArr.stream().map(TokenList::new).collect(Collectors.toCollection(ArrayList::new));
        return tokenListList;
    }
    
    public <U> TokenList<U> castValuesTo(Class<U> toClass) {
        TokenList<U> newTokenList = new TokenList(fromStrBegin);
        for (Token<T> token : this) {
            newTokenList.add(token.castValueTo(toClass));
        }
        return newTokenList;
    }
    
}
