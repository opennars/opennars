package nars.core;

import nars.nal.truth.BasicTruth;
import nars.nal.truth.DefaultTruth;
import nars.nal.truth.Truth;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class TruthTest {

    @Test
    public void testFreqEquality() {
        Truth a = new DefaultTruth(1.0f, 0.9f);
        Truth aCopy = new DefaultTruth(1.0f, 0.9f);
        assertEquals(a, aCopy);

        Truth aEqualWithinThresh = new DefaultTruth(1.0f- DefaultTruth.DEFAULT_TRUTH_EPSILON /2f, 0.9f);
        assertEquals(a, aEqualWithinThresh);

        Truth aNotWithinThresh = new DefaultTruth(1.0f - DefaultTruth.DEFAULT_TRUTH_EPSILON *1f, 0.9f);
        assertNotEquals(a, aNotWithinThresh);

    }

    @Test
    public void testConfEquality() {
        Truth a = new DefaultTruth(1.0f, 0.5f);

        Truth aEqualWithinThresh = new DefaultTruth(1.0f, 0.5f- DefaultTruth.DEFAULT_TRUTH_EPSILON /2f);
        assertEquals(a, aEqualWithinThresh);

        Truth aNotWithinThresh = new DefaultTruth(1.0f, 0.5f - DefaultTruth.DEFAULT_TRUTH_EPSILON *1f);
        assertNotEquals(a, aNotWithinThresh);
    }

    @Test public void testEpsilon() {
        float e = 0.1f;

        Truth a = new BasicTruth(1.0f, 0.9f, e);
        assertEquals(a.getEpsilon(), e, 0.0001);

        Truth aCopy = new BasicTruth(1.0f, 0.9f, e);
        assertEquals(a, aCopy);

        Truth aEqualWithinThresh = new BasicTruth(1.0f- a.getEpsilon()/2, 0.9f, e);
        assertEquals(a, aEqualWithinThresh);

        Truth aNotWithinThresh = new BasicTruth(1.0f - a.getEpsilon(), 0.9f, e);
        assertNotEquals(a, aNotWithinThresh);
    }
}
