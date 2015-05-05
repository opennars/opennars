package nars.core;

import nars.Global;
import nars.nal.Truth;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class TruthTest {

    @Test
    public void testFreqEquality() {
        Truth a = new Truth(1.0f, 0.9f);
        Truth aCopy = new Truth(1.0f, 0.9f);
        assertEquals(a, aCopy);

        Truth aEqualWithinThresh = new Truth(1.0f- Global.TRUTH_EPSILON/2f, 0.9f);
        assertEquals(a, aEqualWithinThresh);

        Truth aNotWithinThresh = new Truth(1.0f - Global.TRUTH_EPSILON*1f, 0.9f);
        assertNotEquals(a, aNotWithinThresh);

    }
    @Test
    public void testConfEquality() {
        Truth a = new Truth(1.0f, 0.5f);

        Truth aEqualWithinThresh = new Truth(1.0f, 0.5f- Global.TRUTH_EPSILON/2f);
        assertEquals(a, aEqualWithinThresh);

        Truth aNotWithinThresh = new Truth(1.0f, 0.5f - Global.TRUTH_EPSILON*1f);
        assertNotEquals(a, aNotWithinThresh);
    }

}
