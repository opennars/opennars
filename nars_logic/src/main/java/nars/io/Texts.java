package nars.io;

import nars.util.data.rope.Rope;
import nars.util.data.rope.StringHack;
import org.apache.commons.lang3.StringUtils;

import java.nio.CharBuffer;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Arrays;
import java.util.Locale;

/**
 * Utilities for process Text & String input/output, ex: encoding/escaping and decoding/unescaping Terms 
 */
abstract public class Texts  {
    //TODO find more appropriate symbol mapping
    //TODO escape any mapped characters if they appear in input during encoding
    //http://www.ssec.wisc.edu/~tomw/java/unicode.html
    
//    private final static Map<Character,Character> escapeMap = new HashMap(256);
//    private final static Map<Character,Character> escapeMapReverse = new HashMap(256);
//    static {
//        char[][] escapings = new char[][] {
//            {':', '\u25B8'},
//            {' ', '\u2581'},
//            {'%', '\u25B9'},
//            {'#', '\u25BA'},
//            {'&', '\u25BB'},
//            {'?', '\u25FF'},
//            {'/', '\u279A'},
//            {'=', '\u25BD'},
//            {';', '\u25BE'},
//            {'-', '\u25BF'},
//            {'.', '\u00B8'},
//            {'<', '\u25B4'},
//            {'>', '\u25B5'},
//            {'[', '\u25B6'},
//            {']', '\u25B7'},
//                {'(', '\u26B6'},
//                {')', '\u26B7'},
//                {'{', '\u27B6'},
//                {'}', '\u27B7'},
//            {'$', '\u25B3'}
//        };
//
//        for (final char[] pair : escapings) {
//            Character existing = escapeMap.put(pair[0], pair[1]);
//            if (existing!=null) {
//                System.err.println("escapeMap has duplicate key: " + pair[0] + " can not apply to both " + existing + " and " + pair[1] );
//                System.exit(1);
//            }
//        }
//
//        //generate reverse mapping
//        for (Map.Entry<Character, Character> e : escapeMap.entrySet())
//            escapeMapReverse.put(e.getValue(), e.getKey());
//    }



//    protected static StringBuilder escape(CharSequence s, boolean unescape, boolean useQuotes) {
//        StringBuilder b = new StringBuilder(s.length());
//
//
//        final Map<Character,Character> map = unescape ? escapeMapReverse : escapeMap;
//
//        boolean inQuotes = !useQuotes;
//        char lastChar = 0;
//
//        for (int i = 0; i < s.length(); i++) {
//            char c = s.charAt(i);
//
//
//            if (c == Symbols.QUOTE) {
//                b.append(Symbols.QUOTE);
//
//                if (useQuotes) {
//                    if (lastChar != '\\')
//                        inQuotes = !inQuotes;
//                }
//
//                continue;
//            }
//
//            if (!inQuotes) {
//                b.append(c);
//                continue;
//            }
//
//            Character d = map.get(c);
//            if (d == null)
//                d = c;
//            b.append(d);
//
//            if (unescape)
//                lastChar = d;
//            else
//                lastChar = c;
//        }
//        return b;
//    }
//
//    /** returns an escaped representation for input.  ranges that begin and end with Symbols.QUOTE are escaped, otherwise the string is not modified.
//     */
//    public static StringBuilder escape(CharSequence s) {
//        return escape(s, false, true);
//    }
//
//    /** returns an unescaped represntation of input */
//    public static StringBuilder unescape(CharSequence s) {
//        return escape(s, true, true);
//    }
    
    
    
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

//    /** escapeLiteral does not involve quotes. this can be used to escape characters directly.*/
//    public static StringBuilder escapeLiteral(CharSequence s) {
//        return escape(s, false, false);
//    }
//
//    /** unescapeLiteral does not involve quotes. this can be used to unescape characters directly.*/
//    public static StringBuilder unescapeLiteral(CharSequence s) {
//        return escape(s, true, false);
//    }

    public static char[] getCharArray(String s) {
        return Rope.getCharArray(s);
    }
    public static char[] getCharArray(StringBuilder s) {
        return Rope.getCharArray(s);
    }


    public static int fuzzyDistance(CharSequence a, CharSequence b) {
        return StringUtils.getFuzzyDistance(a, b, Locale.getDefault());
    }

    /*
    public static void main(String[] args) {
    String s = "Immutable";
    String t = "Notreally";
    mutate(s, t);
    StdOut.println(t);
    // strings are interned so this doesn't even print "Immutable" (!)
    StdOut.println("Immutable");
    }
     */
    /**
     * @author http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
     */
    public static int levenshteinDistance(final CharSequence a, final CharSequence b) {
        int len0 = a.length() + 1;
        int len1 = b.length() + 1;
        int[] cost = new int[len0];
        int[] newcost = new int[len0];
        for (int i = 0; i < len0; i++) {
            cost[i] = i;
        }
        for (int j = 1; j < len1; j++) {
            newcost[0] = j;
            final char bj = b.charAt(j - 1);
            for (int i = 1; i < len0; i++) {
                int match = (a.charAt(i - 1) == bj) ? 0 : 1;
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;
                
                int c = cost_insert;
                if (cost_delete < c) c = cost_delete;
                if (cost_replace < c) c = cost_replace;
                
                newcost[i] = c;
            }
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }
        return cost[len0 - 1];
    }

    /** Change the first min(|s|, |t|) characters of s to t
    TODO must reset the hashcode field
    TODO this is untested and probably not yet functional
     */
    public static void overwrite(CharSequence s, CharSequence t) {
        try {
            char[] value = (char[]) StringHack.val.get(s);

            for (int i = 0; i < Math.min(s.length(), t.length()); i++) {
                value[i] = t.charAt(i);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Half-way between a String and a Rope; concatenates a list of strings into an immutable CharSequence which is either:
     *  If a component is null, it is ignored.
     *  if total non-null components is 0, returns null
     *  if total non-null components is 1, returns that component.
     *  if the combined length <= maxLen, creates a StringBuilder appending them all.
     *  if the combined length > maxLen, creates a Rope appending them all.
     * 
     * TODO do not allow a StringBuilder to appear in output, instead wrap in CharArrayRope
     */
    public static CharSequence yarn(final int maxLen, final CharSequence... components) {
        int totalLen = 0;
        int total = 0;
        CharSequence lastNonNull = null;
        for (final CharSequence s : components) {
            if (s != null) {
                totalLen += s.length();
                total++;
                lastNonNull = s;
            }
        }
        if (total == 0) {
            return null;
        }
        if (total == 1) {
            return lastNonNull.toString();
        }
        
        if ((totalLen <= maxLen) || (maxLen == -1)) {            
            /*
            final CharBuffer n = CharBuffer.allocate(totalLen);

            for (final CharSequence s : components) {            
                n.append(s);
            }

            return n.compact();
            */
        
            
            StringBuilder sb = new StringBuilder(totalLen);
            for (final CharSequence s : components) {
                if (s != null) {
                    sb.append(s);
                }
            }
            return Rope.sequence(sb);
        } else {
            Rope r = Rope.catFast(components);
            return r;
        }
    }    
    
    public static boolean containsChar(final CharSequence n, final char c) {
        if (n instanceof String)
            return ((String)n).indexOf(c)!=-1;
        
        final int l = n.length();
        for (int i = 0; i < l; i++)            
            if (n.charAt(i) == c)
                return true;        
        return false;
    }



    final static Format oneDecimal = new DecimalFormat("0.0");
    public static final String n1(final float x) { return oneDecimal.format(x);     }
    public static final String n1char(final double x) { return oneDecimal.format(x);     }

    final static Format threeDecimal = new DecimalFormat("0.000");
    public static final String n3(final float x) { return threeDecimal.format(x);     }
    public static final String n3(final double x) { return threeDecimal.format(x);     }

    final static Format fourDecimal = new DecimalFormat("0.0000");
    public static final String n4(final float x) { return fourDecimal.format(x);     }
    public static final String n4(final double x) { return fourDecimal.format(x);     }

    final static Format twoDecimal = new DecimalFormat("0.00");    
    public static final String n2Slow(final float x) { return twoDecimal.format(x);     }

    public static long thousandths(final float d) {
        return (long) ((d * 1000.0f + 0.5f));
    }
    public static long hundredths(final float d) {
        return (long) ((d * 100.0f + 0.5f));
    }
    public static int tens(final float d) {
        return (int) ((d * 10.0f + 0.5f));
    }

    /** 2 decimal representation of values between 0 and 1. only the tens and hundredth
     * decimal point are displayed - not the ones, and not a decimal point.
     * for compact display.
     * if the value=1.0, then 'aa' is the result
     */
    public static final String n2u(final float x) {
        if ((x < 0) || (x > 1)) throw new RuntimeException("values >=0 and <=1");
        int hundreds = (int)hundredths(x);
        if (x == 100) return "aa";
        if (hundreds < 10) return "0" + hundreds;
        else
            return Integer.toString(hundreds);
    }

    public static final CharSequence n2(final float x) {
        if ((x < 0) || (x > 1.0f))
            throw new RuntimeException("Invalid value for Texts.n2: " + x);
        
        int hundredths = (int)hundredths(x);
        switch (hundredths) {
            //some common values
            case 100: return "1.00";
            case 99: return "0.99";
            case 90: return "0.90";
            case 0: return "0.00";
        }
                    
        if (hundredths > 9) {
            int tens = hundredths/10;
            return new String(new char[] {
                '0', '.', (char)('0' + tens), (char)('0' + hundredths%10)
            });
        }
        else {
            return new String(new char[] {
                '0', '.', '0', (char)('0' + hundredths)
            });
        }            
    }
    
    //final static Format oneDecimal = new DecimalFormat("0.0");
    /** 1 character representing a 1 decimal of a value between 0..1.0;
     *  representation; 0..9 //, A=1.0 */
    public static final char n1char(final float x) {
        int i = tens(x);
        if (i >= 10)
            i=9; //return 'A';
        return (char)('0' + i);
    }

    public static int compare(final CharSequence s, final CharSequence t) {
        if ((s instanceof String) && (t instanceof String)) {
            return ((String)s).compareTo((String)t);
        }
        else if ((s instanceof CharBuffer) && (t instanceof CharBuffer)) {
            return ((CharBuffer)s).compareTo((CharBuffer)t);
        }
        
        int i = 0;

        final int sl = s.length();
        final int tl = t.length();
        
        while (i < sl && i < tl) {
            char a = s.charAt(i);
            char b = t.charAt(i);

            int diff = a - b;

            if (diff != 0)
              return diff;

            i++;
        }

        return sl - tl;
      }

    public static CharSequence n2(final double p) {
        return n2((float)p);
    }


    /** character to a digit, or -1 if it wasnt a digit */
    public static int i(final char c) {
        if ((c >= '0' && c <= '9'))
            return c - '0';
        return -1;
    }

    /** fast parse an int under certain conditions, avoiding Integer.parse if possible */
    public static int i(final String s) {
        if (s.length() == 1) {
            char c = s.charAt(0);
            int i = i(c);
            if (i!=-1) return i;
        }
        else if (s.length() == 2) {
            int dig1 = i(s.charAt(1));
            int dig10 = i(s.charAt(0));
            if ((dig1!=-1) && (dig10!=-1))
                return dig10*10+dig1;
        }
        return Integer.parseInt(s);
    }

    /** fast parse for float, checking common conditions */
    public static float f(final String s) {

        switch(s) {
            case "0": return 0;
            case "0.00": return 0;
            case "1": return 1f;
            case "1.00": return 1f;
            case "0.90": return 0.9f;
            case "0.9": return 0.9f;
            case "0.5": return 0.5f;
            default: return Float.parseFloat(s);
        }

    }

    public static float f(final String s, final float min, final float max) {
        float x = f(s);
        if ((x < min) || x > max)
            return Float.NaN;
        return x;
    }

    public static String arrayToString(final Object... signals) {
        if (signals == null) return "";
        final int slen = signals.length;
        if ((signals != null) && (slen > 1))
            return Arrays.toString(signals);
        else if (slen > 0)
            return signals[0].toString();
        return "";
    }

    public static CharSequence n(float v, int decimals) {

        switch (decimals) {
            case 1: return n1(v);
            case 2: return n2(v);
            case 3: return n3(v);
            case 4: return n4(v);
        }

        throw new RuntimeException("invalid decimal number");
    }

//    /** fast append to CharBuffer */
//    public final static CharBuffer append(final CharBuffer c, final CharSequence s) {
//        if (s instanceof CharBuffer) {            
//            
//            c.append((CharBuffer)s);
//            return c;
//        }
//        else if (s instanceof String) {
//            //c.put(getCharArray((String)s), 0, s.length());            
//            return c.append(s);
//        }
//        else {
//            return c.append(s);
//        }
//    }
    
//    public final static CharBuffer append(final CharBuffer c, final CharBuffer s) {
//        return c.put(s);        
//    }
//    public final static CharBuffer append(final CharBuffer c, final String s) {
//        return c.put(getCharArray(s));        
//    }
//    public final static CharBuffer append(final CharBuffer b, final char c) {
//        return b.put(c);        
//    }

}
