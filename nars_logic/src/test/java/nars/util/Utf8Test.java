/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util;

import nars.util.utf8.Utf8;
import org.junit.Test;

import java.util.Arrays;

import static nars.util.utf8.Utf8.bytesToChars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class Utf8Test {
    public static byte[] charsToBytes(char[] s) {
        int slen = s.length;
        byte[] b = new byte[slen *2];
        int j = 0;
        for (char c : s) {
            b[j++] = (byte) ((c & 0xFF00) >> 8);
            b[j++] = (byte) ((c & 0x00FF));
        }
        return b;
    }

    @Test
    public void testUTF8() {
        Utf8 u = new Utf8("x");
        assertEquals(1, u.length());
        assertEquals("x", u.toString());
        assertEquals('x', u.charAt(0));
        assertEquals(151, u.hashCode());
        
        Utf8 v = new Utf8("string");
        assertEquals(6, v.length());
        
        assertEquals(0, new Utf8("abc").compareTo(new Utf8("abc")));
        assertEquals(1, new Utf8("abc").compareTo(new Utf8("abd")));
        assertEquals(-1, new Utf8("abc").compareTo(new Utf8("abb")));
        assertEquals(-1, new Utf8("abc").compareTo(new Utf8("ab")));
    }

    @Test public void testCharByteConvert() {
        byte[] x = { 1, 2, 3, 4, 5, 6, 7, 8 };
        char[] y = bytesToChars(x);
        assertEquals(x.length/2, y.length);
        byte[] x2 = charsToBytes(y);
        //System.out.println(Arrays.toString(x2));
        assertEquals(x[1], x2[1]);
        assertEquals(x[0], x2[0]);
        assertTrue(Arrays.equals(x, x2));
    }

//    @Test public void testStringUTF8CasesOdd() {
//        testStringUTF8("abc"); //odd #
//    }
//    @Test public void testStringUTF8CasesEven() {
//        testStringUTF8("ab"); //even #
//    }
//
//    public void testStringUTF8(String x) {
//        //String x = "abcdefgh";
//
//
//        String y = Utf8.fromStringtoStringUtf8(x);
//        {
//            int expLen = x.length() / 2;
//
//            assertTrue( y.length()==expLen || y.length()==(expLen+1) );
//
//            //System.out.println(Arrays.toString(toUtf8(x)));
//            //System.out.println(Arrays.toString(fromUtf8ToChars(toUtf8(x))));
//            char[] utf8Chars = fromUtf8ToChars(toUtf8(x));
//            assertTrue(Arrays.equals(x.toCharArray(), utf8Chars));
//
//            //System.out.println(Arrays.toString(bytesToChars(toUtf8(x))));
//            //System.out.println(y.length() + " " + y);
//        }
//        String x2 = Utf8.fromStringUtf8(y);
//        System.out.println(x2);
//        assertEquals(x, x2);
//
//    }
}
