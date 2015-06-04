package nars.core;

import nars.NAR;
import nars.model.impl.Default;
import nars.util.data.id.Identifier;
import nars.util.data.id.UTF8Identifier;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 6/3/15.
 */
public class TermIDTest {

    final static NAR nar = new NAR( new Default().level(1) );

    @Test
    public void testInternalRepresentation() {
        testInternalRepresentation("x", 1);
        testInternalRepresentation("xyz", 3);
        testInternalRepresentation("\u00ea", 2);
        testInternalRepresentation("xyz\u00e3", 3 + 2);

        //5 bytes: '<', 'a', [code for inheritance], 'b', '>'
        //tests whether the relatoin symbol has been reduced to the
        // control character that represents it
        testInternalRepresentation("<a --> b>", 5);
        testInternalRepresentation("(&&, a, b)", 7);
        //testInternalRepresentation("<a && b>", 5);
        testInternalRepresentation("(--,a)", 2);
    }

    public void testInternalRepresentation(String expectedStringOutput, int expectedLength) {
        testInternalRepresentation(null, expectedStringOutput, expectedLength);
    }

    public Identifier testInternalRepresentation(String expectedCompactOutput, String expectedPrettyOutput, int expectedLength) {
        //UTF8Identifier b = new UTF8Identifier(expectedPrettyOutput);
        Identifier i = nar.term(expectedPrettyOutput).name();
        byte[] b = i.bytes();

        if (expectedCompactOutput != null)
            assertEquals(expectedCompactOutput, i.toString(false));

        areEqualAndIfNotWhy(expectedPrettyOutput, i.toString());


        assertEquals(Arrays.toString(b), expectedLength, b.length);
        return i;
    }

    public void areEqualAndIfNotWhy(String a, String b) {
        assertEquals(charComparison(a, b), a, b);
    }

    public static String charComparison(String a, String b) {
        return Arrays.toString(a.toCharArray()) + " != " + Arrays.toString(b.toCharArray());
    }
}
