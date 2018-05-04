/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.io;

import java.nio.CharBuffer;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for process Text & String input/output, ex: encoding/escaping and decoding/unescaping Terms 
 */
public class Texts {
    //TODO find more appropriate symbol mapping
    //TODO escape any mapped characters if they appear in input during encoding
    //http://www.ssec.wisc.edu/~tomw/java/unicode.html
    
    public final static Map<Character,Character> escapeMap = new HashMap(256);
    public final static Map<Character,Character> escapeMapReverse = new HashMap(256);
    static {
        char[][] escapings = new char[][] {
            {':', '\u25B8'},
            {' ', '\u2581'},
            {'%', '\u25B9'}, 
            {'#', '\u25BA'}, 
            {'&', '\u25BB'}, 
            {'?', '\u25FF'}, 
            {'/', '\u279A'}, 
            {'=', '\u25BD'}, 
            {';', '\u25BE'}, 
            {'-', '\u25BF'},   
            {'.', '\u00B8'},
            {'<', '\u25B4'},
            {'>', '\u25B5'},
            {'[', '\u25B6'},
            {']', '\u25B7'},
            {'$', '\u25B3'}
        };
        
        for (final char[] pair : escapings) {
            Character existing = escapeMap.put(pair[0], pair[1]);
            if (existing!=null) {
                System.err.println("escapeMap has duplicate key: " + pair[0] + " can not apply to both " + existing + " and " + pair[1] );
                //--System.exit(1);
            }
        }

        //generate reverse mapping
        for (Map.Entry<Character, Character> e : escapeMap.entrySet())
            escapeMapReverse.put(e.getValue(), e.getKey());
    }
    

    protected static StringBuilder escape(CharSequence s, boolean unescape, boolean useQuotes) {       
        StringBuilder b = new StringBuilder(s.length());
        
        
        final Map<Character,Character> map = unescape ? escapeMapReverse : escapeMap;
        
        boolean inQuotes = !useQuotes;
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

    /** escapeLiteral does not involve quotes. this can be used to escape characters directly.*/
    public static StringBuilder escapeLiteral(CharSequence s) {
        return escape(s, false, false);
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
    public static CharSequence yarn(final CharSequence... components) {
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
        
        StringBuilder sb = new StringBuilder(totalLen);
        for (final CharSequence s : components) {
            if (s != null) {
                sb.append(s);
            }
        }
        return sb;
    }    

    final static Format fourDecimal = new DecimalFormat("0.0000");
    public static final String n4(final float x) { return fourDecimal.format(x);     }

    final static Format twoDecimal = new DecimalFormat("0.00");    
    public static final String n2Slow(final float x) { return twoDecimal.format(x);     }

    public static long thousandths(final float d) {
        return (long) ((d * 1000f + 0.5f));
    }
    public static long hundredths(final float d) {
        return (long) ((d * 100f + 0.5f));
    }
     
    public static final CharSequence n2(final float x) {         
        if ((x < 0) || (x > 1.0f))
            throw new RuntimeException("Invalid value for Texts.n2");
        
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
    
    final static Format oneDecimal = new DecimalFormat("0.0");    
    public static final String n1(final float x) { return oneDecimal.format(x);     }

    public static int compareTo(final CharSequence s, final CharSequence t) {
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

}
