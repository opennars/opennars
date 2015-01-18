/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util;

import nars.util.data.Utf8;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author me
 */
public class Utf8Test {
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
}
