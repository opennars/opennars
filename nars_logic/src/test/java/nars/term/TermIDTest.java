package nars.term;

import nars.NAR;
import nars.nar.Terminal;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 6/3/15.
 */
public class TermIDTest {

    static final NAR nar = new Terminal();


    /* i will make these 3 pass soon, this is an improvement on the representation
    that will make these tests pass once implemented. */

    // '&&' 'a' ',' 'b' ')'
    @Test
    public void testInternalRepresentation28() {
        testBytesRepresentation("(&&,a,b)", 5);
    }

    // '--', 'a'
    @Test
    public void testInternalRepresentation29() {
        testBytesRepresentation("(--,a)", 2);
    }

    // '*' 'a' ',' 'b' ')'
    @Test
    public void testInternalRepresentation2z() {
        testBytesRepresentation("(a,b)", 5);
    }



    /**
     * tests whether NALOperators has been reduced to the
     * compact control character (8bits UTF) that represents it
     */

    @Test
    public void testInternalRepresentation23() {
        testBytesRepresentation("x", 1);
    }

    @Test
    public void testInternalRepresentation24() {
        testBytesRepresentation("xyz", 3);
    }

    @Test
    public void testInternalRepresentation25() {
        testBytesRepresentation("\u00ea", 2);
    }

    @Test
    public void testInternalRepresentation26() {
        testBytesRepresentation("xyz\u00e3", 3 + 2);
    }

    //  '-->' 'a' ','  'b' ')' == 5
    @Test
    public void testInternalRepresentation27() {
        testBytesRepresentation("<a-->b>", 5);
    }

    @Test public void testInternalRepresentationImage1() {
        for (char t : new char[] { '/', '\\'}) {
            testBytesRepresentation("("+t+",_,a)", 3 + 1);
            testBytesRepresentation("("+t+",_,a,b)", 5 + 1);
            testBytesRepresentation("("+t+",a,_,b)", 5 + 1);
            testBytesRepresentation("("+t+",a,b,_)", 5 + 1);
        }
    }


    //@Test public void testInternalRepresentation2() { testInternalRepresentation("<a && b>", 5); }


    public Term testBytesRepresentation(String expectedCompactOutput, int expectedLength) {
        return testBytesRepresentation(
            null,
            expectedCompactOutput,
            expectedLength);
    }

    public Term testBytesRepresentation(String expectedCompactOutput, String expectedPrettyOutput, int expectedLength) {
        //UTF8Identifier b = new UTF8Identifier(expectedPrettyOutput);
        Term i = nar.term(expectedPrettyOutput);
        //byte[] b = i.bytes();
        //byte[] b = i.bytes();

        if (expectedCompactOutput != null)
            assertEquals(expectedCompactOutput, i.toString(false));

        areEqualAndIfNotWhy(expectedPrettyOutput, i.toString());


        //assertEquals(expectedCompactOutput + " ---> " + Arrays.toString(b), expectedLength, b.length);
        return i;
    }

    public void areEqualAndIfNotWhy(String a, String b) {
        assertEquals(charComparison(a, b), a, b);
    }

    public static String charComparison(String a, String b) {
        return Arrays.toString(a.toCharArray()) + " != " + Arrays.toString(b.toCharArray());
    }

//    @Test public void testComparingStringAndUtf8Atoms() {
//        testStringUtf8Equal("x");
//        testStringUtf8Equal("xy");
//        testStringUtf8Equal("xyz");
//        testTermInEqual(new StringAtom("x"), new Utf8Atom("y"));
//        testTermInEqual($.$("$x"), new Utf8Atom("x"));
//        testTermInEqual($.$("$x"), new StringAtom("x"));
//    }
//
//    public void testStringUtf8Equal(String id) {
//        StringAtom s = new StringAtom(id);
//        Utf8Atom u = new Utf8Atom(id);
//
//        assertEquals(id, u.toString());
//        assertEquals(id, s.toString());
//        assertEquals(Op.ATOM, u.op());
//
//        testTermEqual(u, s);
//        assertEquals(0, u.compareTo(s));
//        assertEquals(0, s.compareTo(u));
//        assertEquals(id.hashCode(), s.hashCode());
//        assertEquals(u.hashCode(), s.hashCode());
//
//    }
//
//    public void testTermInEqual(Term u, Term s) {
//
//        int us = u.compareTo(s);
//        assertNotEquals(0, us);
//        assertEquals(-us, s.compareTo(u));
//        assertNotEquals(u.hashCode(), s.hashCode());
//
//    }
//    public void testTermEqual(Term u, Term s) {
//
//        assertEquals(u.op(), s.op());
//
//        assertEquals(u.hashCode(), s.hashCode());
//
//        assertEquals(u, s);
//
//
//    }
}
