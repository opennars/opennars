package nars.io;

/**
 * Utilities for encoding/escaping and decoding/unescaping Terms 
 */
public class Termize {
    //http://www.ssec.wisc.edu/~tomw/java/unicode.html
    //TODO use a table for both the enoding and decoding
    
    public static String enterm(String s) {
        return s.replaceAll(":", "\u25B8")
                .replaceAll(" ", "\u2581")
                .replaceAll("%", "\u25B9") //TODO find a different unicode char
                .replaceAll("#", "\u25BA") //TODO find a different unicode char
                .replaceAll("&", "\u25BB") //TODO find a different unicode char
                .replaceAll("\\?", "\u25FF") //TODO find a different unicode char
                .replaceAll("/", "\u279A") //TODO find a different unicode char
                .replaceAll("=", "\u25BD") //TODO find a different unicode char
                .replaceAll(";", "\u25BE") //TODO find a different unicode char
                .replaceAll("-", "\u25BF") //TODO find a different unicode char                
                .replaceAll("\\.", "\u00B8") //TODO find a different unicode char
                ;
    
    }
    
    public static String determ(String t) {
        //TODO
        return null;
    }
}
