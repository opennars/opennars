package objenome.op.cas.util;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    
    public String[][] tokens;
    public String noTokenMsg = "invalid token";
    
    public boolean debug = false;
    
    public Tokenizer(String[][] tokens) {
        this.tokens = tokens;
    }
    
    public TokenList<Object> tokenize(String string) throws ParseException {
        if (debug) System.err.println("tokenizing \"" + string + '"');
        String string2 = string;
        TokenList<Object> tokened = new TokenList<>(0);
        int indexAt = 0;
        
        while (indexAt < string.length()) {
        
            int tokenOnIndex = 0;
            boolean keepGoing = true;
            
            while (keepGoing && tokenOnIndex < Array.getLength(tokens)) {
                Pattern pattern = Pattern.compile('^' +tokens[tokenOnIndex][0]);
                Matcher matcher = pattern.matcher(string2);
                
                if (matcher.find()) {
                    String matched = matcher.group();
                    String token = tokens[tokenOnIndex][1];
                    
                    tokened.add(new Token<>(token, string, indexAt, indexAt + matcher.end()));
                    
                    keepGoing = false;
                    
                    indexAt+= matcher.end() - matcher.start();
                    string2 = string2.substring(matcher.end(), string2.length());
                    
                    // if (debug) System.err.println("last token: " +  tokened.get(tokened.size() - 1));
                    // if (debug) System.err.println("indexAt next: " +  indexAt);
                }
                tokenOnIndex++;
            }
            
            if (keepGoing) {
                throw new ParseException(noTokenMsg, indexAt);
            }
        }
        
        if (debug) System.err.println("tokens:    " + tokened);
        return tokened;
    }
    
    public Tokenizer withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
    
}
