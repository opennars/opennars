package nars.core;

import nars.Global;
import nars.nal.DefaultTruth;
import nars.nal.Truth;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class TruthTest {

    @Test
    public void testFreqEquality() {
        Truth a = new DefaultTruth(1.0f, 0.9f);
        Truth aCopy = new DefaultTruth(1.0f, 0.9f);
        assertEquals(a, aCopy);

        Truth aEqualWithinThresh = new DefaultTruth(1.0f- Global.DEFAULT_TRUTH_EPSILON /2f, 0.9f);
        assertEquals(a, aEqualWithinThresh);

        Truth aNotWithinThresh = new DefaultTruth(1.0f - Global.DEFAULT_TRUTH_EPSILON *1f, 0.9f);
        assertNotEquals(a, aNotWithinThresh);

    }
    @Test
    public void testConfEquality() {
        Truth a = new DefaultTruth(1.0f, 0.5f);

        Truth aEqualWithinThresh = new DefaultTruth(1.0f, 0.5f- Global.DEFAULT_TRUTH_EPSILON /2f);
        assertEquals(a, aEqualWithinThresh);

        Truth aNotWithinThresh = new DefaultTruth(1.0f, 0.5f - Global.DEFAULT_TRUTH_EPSILON *1f);
        assertNotEquals(a, aNotWithinThresh);
    }

    @Test public void testEpsilon() {
        float e = 0.1f;
        DefaultTruth a = new DefaultTruth(1.0f, 0.9f, e);
        DefaultTruth aCopy = new DefaultTruth(1.0f, 0.9f, e);
        assertEquals(a, aCopy);

        Truth aEqualWithinThresh = new DefaultTruth(1.0f- a.getEpsilon()/2, 0.9f, e);
        assertEquals(a, aEqualWithinThresh);

        Truth aNotWithinThresh = new DefaultTruth(1.0f - a.getEpsilon(), 0.9f, e);
        assertNotEquals(a, aNotWithinThresh);
    }
}
