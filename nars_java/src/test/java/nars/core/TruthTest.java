package nars.core;

import nars.Global;
import nars.nal.TruthValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class TruthTest {

    @Test
    public void testFreqEquality() {
        TruthValue a = new TruthValue(1.0f, 0.9f);
        TruthValue aCopy = new TruthValue(1.0f, 0.9f);
        assertEquals(a, aCopy);

        TruthValue aEqualWithinThresh = new TruthValue(1.0f- Global.TRUTH_EPSILON/2f, 0.9f);
        assertEquals(a, aEqualWithinThresh);

        TruthValue aNotWithinThresh = new TruthValue(1.0f - Global.TRUTH_EPSILON*1f, 0.9f);
        assertNotEquals(a, aNotWithinThresh);

    }
    @Test
    public void testConfEquality() {
        TruthValue a = new TruthValue(1.0f, 0.5f);

        TruthValue aEqualWithinThresh = new TruthValue(1.0f, 0.5f- Global.TRUTH_EPSILON/2f);
        assertEquals(a, aEqualWithinThresh);

        TruthValue aNotWithinThresh = new TruthValue(1.0f, 0.5f - Global.TRUTH_EPSILON*1f);
        assertNotEquals(a, aNotWithinThresh);
    }

}
