package nars.util;

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
public enum Texts {
    ;


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
    public static int levenshteinDistance(CharSequence a, CharSequence b) {
        int len0 = a.length() + 1;
        int len1 = b.length() + 1;
        int[] cost = new int[len0];
        int[] newcost = new int[len0];
        for (int i = 0; i < len0; i++) {
            cost[i] = i;
        }
        for (int j = 1; j < len1; j++) {
            newcost[0] = j;
            char bj = b.charAt(j - 1);
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

    /**
     * Change the first min(|s|, |t|) characters of s to t
     * TODO must reset the hashcode field
     * TODO this is untested and probably not yet functional
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

    public static boolean containsChar(CharSequence n, char c) {
        if (n instanceof String)
            return ((String) n).indexOf(c) != -1;

        int l = n.length();
        for (int i = 0; i < l; i++)
            if (n.charAt(i) == c)
                return true;
        return false;
    }


    static final ThreadLocal<Format> oneDecimal = ThreadLocal.withInitial( () -> new DecimalFormat("0.0") );

    public static String n1(float x) {
        return oneDecimal.get().format(x);
    }

    public static String n1char(double x) {
        return oneDecimal.get().format(x);
    }


    static final ThreadLocal<Format> threeDecimal = ThreadLocal.withInitial( () -> new DecimalFormat("0.000") );

    public static String n3(float x) {
        return threeDecimal.get().format(x);
    }

    public static String n3(double x) {
        return threeDecimal.get().format(x);
    }

    static final ThreadLocal<Format> fourDecimal = ThreadLocal.withInitial( () -> new DecimalFormat("0.0000") );

    public static String n4(float x) {
        return fourDecimal.get().format(x);
    }

    public static String n4(double x) {
        return fourDecimal.get().format(x);
    }

    static final Format twoDecimal = new DecimalFormat("0.00");

//    public static final String n2Slow(final float x) {
//        return twoDecimal.format(x);
//    }

    public static long thousandths(float d) {
        return (long) ((d * 1000.0f + 0.5f));
    }

    public static long hundredths(float d) {
        return (long) ((d * 100.0f + 0.5f));
    }

    public static int tens(float d) {
        return (int) ((d * 10.0f + 0.5f));
    }

    public static CharSequence n2(float x) {
        if ((x < 0) || (x > 1.0f))
            return twoDecimal.format(x);

        int hundredths = (int) hundredths(x);
        switch (hundredths) {
            //some common values
            case 100:
                return "1.0";
            case 99:
                return ".99";
            case 90:
                return ".90";
            case 0:
                return "0.0";
        }

        if (hundredths > 9) {
            int tens = hundredths / 10;
            return new String(new char[]{
                    '.', (char) ('0' + tens), (char) ('0' + hundredths % 10)
            });
        } else {
            return new String(new char[]{
                    '.', '0', (char) ('0' + hundredths)
            });
        }
    }

    //final static Format oneDecimal = new DecimalFormat("0.0");

    /**
     * 1 character representing a 1 decimal of a value between 0..1.0;
     * representation; 0..9 //, A=1.0
     */
    public static char n1char(float x) {
        int i = tens(x);
        if (i >= 10)
            i = 9; //return 'A';
        return (char) ('0' + i);
    }

    public static int compare(CharSequence s, CharSequence t) {
        if ((s instanceof String) && (t instanceof String)) {
            return ((String) s).compareTo((String) t);
        }
        if ((s instanceof CharBuffer) && (t instanceof CharBuffer)) {
            return ((CharBuffer) s).compareTo((CharBuffer) t);
        }

        int i = 0;

        int sl = s.length();
        int tl = t.length();

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

    public static CharSequence n2(double p) {
        return n2((float) p);
    }


    /**
     * character to a digit, or -1 if it wasnt a digit
     */
    public static int i(char c) {
        if ((c >= '0' && c <= '9'))
            return c - '0';
        return -1;
    }

    /**
     * fast parse an int under certain conditions, avoiding Integer.parse if possible
     */
    public static int i(String s) {
        if (s.length() == 1) {
            char c = s.charAt(0);
            int i = i(c);
            if (i != -1) return i;
        } else if (s.length() == 2) {
            int dig1 = i(s.charAt(1));
            if (dig1!=-1) {
                int dig10 = i(s.charAt(0));
                if (dig10 != -1)
                    return dig10 * 10 + dig1;
            }
        }
        return Integer.parseInt(s);
    }




    /**
     * fast parse a non-negative int under certain conditions, avoiding Integer.parse if possible
     *
     */
    public static int i(String s, int ifMissing) {
        switch (s.length()) {
            case 0: return ifMissing;
            case 1: return i1(s, ifMissing);
            case 2: return i2(s, ifMissing);
            case 3: return i3(s, ifMissing);
            default:
                try {
                    return Integer.parseInt(s);
                }
                catch (NumberFormatException e) {
                    return ifMissing;
                }
        }
    }

    private static int i3(String s, int ifMissing) {
        int dig100 = i(s.charAt(0));
        if (dig100 == -1) return ifMissing;

        int dig10 = i(s.charAt(1));
        if (dig10 == -1) return ifMissing;

        int dig1 = i(s.charAt(2));
        if (dig1 == -1) return ifMissing;

        return dig100 * 100 + dig10 * 10 + dig1;
    }

    private static int i2(String s, int ifMissing) {
        int dig10 = i(s.charAt(0));
        if (dig10 == -1) return ifMissing;

        int dig1 = i(s.charAt(1));
        if (dig1 == -1) return ifMissing;

        return dig10 * 10 + dig1;
    }

    private static int i1(String s, int ifMissing) {
        int dig1 = i(s.charAt(0));
        if (dig1 != -1) return ifMissing;
        return dig1;
    }

    /**
     * fast parse for float, checking common conditions
     */
    public static float f(String s) {

        switch (s) {
            case "0":
                return 0;
            case "0.00":
                return 0;
            case "1":
                return 1.0f;
            case "1.00":
                return 1.0f;
            case "0.90":
                return 0.9f;
            case "0.9":
                return 0.9f;
            case "0.5":
                return 0.5f;
            default:
                return Float.parseFloat(s);
        }

    }

    public static float f(String s, float min, float max) {
        float x = f(s);
        if ((x < min) || x > max)
            return Float.NaN;
        return x;
    }

    public static String arrayToString(Object... signals) {
        if (signals == null) return "";
        int slen = signals.length;
        if ((signals != null) && (slen > 1))
            return Arrays.toString(signals);
        if (slen > 0)
            return signals[0].toString();
        return "";
    }

    public static CharSequence n(float v, int decimals) {

        switch (decimals) {
            case 1:
                return n1(v);
            case 2:
                return n2(v);
            case 3:
                return n3(v);
            case 4:
                return n4(v);
        }

        throw new RuntimeException("invalid decimal number");
    }

    public static int count(String s, char x) {
        int c = 0;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == x)
                c++;

        return c;
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
