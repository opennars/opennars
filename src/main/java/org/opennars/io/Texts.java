/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.io;

import java.nio.CharBuffer;
import java.text.DecimalFormat;
import java.text.Format;

/**
 * Utilities for process Text &amp; String input/output, ex: encoding/escaping and decoding/unescaping Terms
 *
 *
 */
public class Texts {

    /** Half-way between a String and a Rope; concatenates a list of strings into an immutable CharSequence which is either:
     *  If a component is null, it is ignored.
     *  if total non-null components is 0, returns null
     *  if total non-null components is 1, returns that component.
     *  if the combined length &lt;= maxLen, creates a StringBuilder appending them all.
     *  if the combined length &gt; maxLen, creates a Rope appending them all.
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
        
        final StringBuilder sb = new StringBuilder(totalLen);
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
            throw new IllegalStateException("Invalid value for Texts.n2");
        
        final int hundredths = (int)hundredths(x);
        switch (hundredths) {
            //some common values
            case 100: return "1.00";
            case 99: return "0.99";
            case 90: return "0.90";
            case 0: return "0.00";
        }
                    
        if (hundredths > 9) {
            final int tens = hundredths/10;
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
            final char a = s.charAt(i);
            final char b = t.charAt(i);

            final int diff = a - b;

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
