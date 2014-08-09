package nars.io;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for process Text input/output, ex: encoding/escaping and decoding/unescaping Terms 
 */
public class Texts {
    //http://www.ssec.wisc.edu/~tomw/java/unicode.html
    //TODO use a table for both the enoding and decoding
    
    public final static Map<Character,Character> escapeMap = new HashMap();
    public final static Map<Character,Character> escapeMapReverse = new HashMap();
    static {
        escapeMap.put(':', '\u25B8');
        escapeMap.put(' ', '\u2581');
        escapeMap.put('%', '\u25B9'); 
        escapeMap.put('#', '\u25BA'); 
        escapeMap.put('&', '\u25BB'); 
        escapeMap.put('?', '\u25FF'); 
        escapeMap.put('/', '\u279A'); 
        escapeMap.put('=', '\u25BD'); 
        escapeMap.put(';', '\u25BE'); 
        escapeMap.put('-', '\u25BF');   
        escapeMap.put('.', '\u00B8');
        escapeMap.put('<', '\u25B4');
        escapeMap.put('>', '\u25B5');
        escapeMap.put('[', '\u25B6');
        escapeMap.put(']', '\u25B7');
        escapeMap.put('$', '\u25B3');
        
        for (Map.Entry<Character, Character> e : escapeMap.entrySet())
            escapeMapReverse.put(e.getValue(), e.getKey());
    }

//    public static String determ(String t) {
//        //TODO
//        return null;
//    }

    protected static StringBuilder escape(CharSequence s, boolean unescape, boolean useQuotes) {       
        StringBuilder b = new StringBuilder(s.length());
        
        final Map<Character,Character> map = unescape ? escapeMapReverse : escapeMap;
        
        boolean inQuotes = useQuotes ? false : true;
        char lastChar = 0;
        
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            
            
            if (c == Symbols.QUOTE) {
                b.append(Symbols.QUOTE);
                
                if (useQuotes) {
                    if (lastChar != '\\')
                        inQuotes = !inQuotes;
                }
                
                continue;
            }
            
            if (!inQuotes) {
                b.append(c);
                continue;
            }
            
            Character d = map.get(c);
            if (d == null)
                d = c;
            b.append(d);

            if (unescape)
                lastChar = d;
            else
                lastChar = c;
        }
        return b;
    }

    /** returns an escaped representation for input.  ranges that begin and end with Symbols.QUOTE are escaped, otherwise the string is not modified.
     */    
    public static StringBuilder escape(CharSequence s) {
        return escape(s, false, true);
    }

    /** returns an unescaped represntation of input */
    public static StringBuilder unescape(CharSequence s) {
        return escape(s, true, true);
    }
    
    
    
//    
//    public static String enterm(String s) {
//        return s.replaceAll(":", "\u25B8")
//                .replaceAll(" ", "\u2581")
//                
//                .replaceAll(">", "\u25B5") //TODO find a different unicode char
//                .replaceAll("[", "\u25B6") //TODO find a different unicode char
//                .replaceAll("]", "\u25B7") //TODO find a different unicode char
//                .replaceAll("$", "\u25B8") //TODO find a different unicode char
//                .replaceAll("%", "\u25B9") //TODO find a different unicode char
//                .replaceAll("#", "\u25BA") //TODO find a different unicode char
//                .replaceAll("&", "\u25BB") //TODO find a different unicode char
//                .replaceAll("\\?", "\u25FF") //TODO find a different unicode char
//                .replaceAll("/", "\u279A") //TODO find a different unicode char
//                .replaceAll("=", "\u25BD") //TODO find a different unicode char
//                .replaceAll(";", "\u25BE") //TODO find a different unicode char
//                .replaceAll("-", "\u25BF")   
//                .replaceAll("\\.", "\u00B8") //TODO find a different unicode char
//                ;
//    
//    }
//        

    /** escapeLiteral does not involve quotes. this can be used to escape characters directly.*/
    public static StringBuilder escapeLiteral(CharSequence s) {
        return escape(s, false, false);
    }
    
    /** unescapeLiteral does not involve quotes. this can be used to unescape characters directly.*/
    public static StringBuilder unescapeLiteral(CharSequence s) {
        return escape(s, true, false);        
    }    
}
