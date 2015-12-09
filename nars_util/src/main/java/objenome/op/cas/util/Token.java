package objenome.op.cas.util;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Token<T> {
    
    public T tokenValue;
    public String fromStr = null;
    public Integer fromStrBegin = null;
    public Integer fromStrEnd = null;
    
    public Token(T tokenValue, String fromStr, int fromStrBegin, int fromStrEnd) {
        this.tokenValue = tokenValue;
        this.fromStr = fromStr;
        this.fromStrBegin = fromStrBegin;
        this.fromStrEnd = fromStrEnd;
    }
    
    public Token(T tokenValue, Token fromStrOffsetToken, Token fromStrEndToken) {
        this.tokenValue = tokenValue;
        fromStr = fromStrOffsetToken.fromStr;
        fromStrBegin = fromStrOffsetToken.fromStrBegin;
        fromStrEnd = fromStrEndToken.fromStrEnd;
    }
    
    public Token(T tokenValue) {
        this.tokenValue = tokenValue;
    }
    
    public boolean valueEquals(Object o) {
        return tokenValue.equals(o);
    }
    
    public String toString() {
        return "([" + tokenValue + ']'
                + (fromStr != null && fromStrBegin != null && fromStrEnd != null
                 ? '"' + fromStr.substring(fromStrBegin, fromStrEnd) + '"' : "") + ')';
    }
    
    public static <T> ArrayList<T> getValues(ArrayList<Token<T>> tokens) {
        ArrayList<T> values = tokens.stream().map(token -> token.tokenValue).collect(Collectors.toCollection(ArrayList::new));
        return values;
    }
    
    public <U> Token<U> castValueTo(Class<U> toClass) {
        return new Token<>((U) tokenValue, fromStr, fromStrBegin, fromStrEnd);
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Token)) return false;
        
        return ((Token) o).tokenValue.equals(tokenValue);
    }
    
    public Token expandIndices(int left, int right) {
        fromStrBegin-= left;
        fromStrEnd+= right;
        return this;
    }
    
    public String fromStrPart() {
        return fromStr.substring(fromStrBegin, fromStrEnd);
    }
    
}
