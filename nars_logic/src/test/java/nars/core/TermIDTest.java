package nars.core;

import nars.NAR;
import nars.nar.Default;
import nars.util.data.id.Identifier;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 6/3/15.
 */
public class TermIDTest {

    final static NAR nar = new NAR(new Default().level(1));


    /* i will make these 3 pass soon, this is an improvement on the representation
    that will make these tests pass once implemented. */

    // '&&' 'a' ',' 'b' ')'
    @Test
    public void testInternalRepresentation28() {
        testInternalRepresentation("(&&, a, b)", 5);
    }

    // '--', 'a'
    @Test
    public void testInternalRepresentation29() {
        testInternalRepresentation("(--,a)", 2);
    }

    // '*' 'a' ',' 'b' ')'
    @Test
    public void testInternalRepresentation2z() {
        testInternalRepresentation("(a, b)", 5);
    }



    /**
     * tests whether NALOperators has been reduced to the
     * compact control character (8bits UTF) that represents it
     */

    @Test
    public void testInternalRepresentation23() {
        testInternalRepresentation("x", 1);
    }

    @Test
    public void testInternalRepresentation24() {
        testInternalRepresentation("xyz", 3);
    }

    @Test
    public void testInternalRepresentation25() {
        testInternalRepresentation("\u00ea", 2);
    }

    @Test
    public void testInternalRepresentation26() {
        testInternalRepresentation("xyz\u00e3", 3 + 2);
    }

    //  '-->' 'a' ','  'b' ')' == 5
    @Test
    public void testInternalRepresentation27() {
        testInternalRepresentation("<a --> b>", 5);
    }




    //@Test public void testInternalRepresentation2() { testInternalRepresentation("<a && b>", 5); }


    public Identifier testInternalRepresentation(String expectedPrettyOutput, int expectedLength) {
        return testInternalRepresentation(null, expectedPrettyOutput, expectedLength);
    }

    public Identifier testInternalRepresentation(String expectedCompactOutput, String expectedPrettyOutput, int expectedLength) {
        //UTF8Identifier b = new UTF8Identifier(expectedPrettyOutput);
        Identifier i = nar.term(expectedPrettyOutput).name();
        byte[] b = i.bytes();

        if (expectedCompactOutput != null)
            assertEquals(expectedCompactOutput, i.toString(false));

        areEqualAndIfNotWhy(expectedPrettyOutput, i.toString());


        assertEquals(expectedCompactOutput + " ---> " + Arrays.toString(b), expectedLength, b.length);
        return i;
    }

    public void areEqualAndIfNotWhy(String a, String b) {
        assertEquals(charComparison(a, b), a, b);
    }

    public static String charComparison(String a, String b) {
        return Arrays.toString(a.toCharArray()) + " != " + Arrays.toString(b.toCharArray());
    }

}
