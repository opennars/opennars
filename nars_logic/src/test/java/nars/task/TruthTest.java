package nars.task;

import nars.truth.DefaultTruth;
import nars.truth.Truth;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class TruthTest {

    @Test
    public void testFreqEquality() {
        Truth a = new DefaultTruth(1.0f, 0.9f);
        Truth aCopy = new DefaultTruth(1.0f, 0.9f);
        assertEquals(a, aCopy);

        Truth aEqualWithinThresh = new DefaultTruth(1.0f- DefaultTruth.DEFAULT_TRUTH_EPSILON / 2.0f, 0.9f);
        assertEquals(a, aEqualWithinThresh);
        assertEquals(a.hashCode(), aEqualWithinThresh.hashCode());

        Truth aNotWithinThresh = new DefaultTruth(1.0f - DefaultTruth.DEFAULT_TRUTH_EPSILON * 1.0f, 0.9f);
        assertNotEquals(a, aNotWithinThresh);
        assertNotEquals(a.hashCode(), aNotWithinThresh.hashCode());

    }

    @Test
    public void testConfEquality() {
        Truth a = new DefaultTruth(1.0f, 0.5f);

        Truth aEqualWithinThresh = new DefaultTruth(1.0f, 0.5f- DefaultTruth.DEFAULT_TRUTH_EPSILON / 2.0f);
        assertEquals(a, aEqualWithinThresh);
        assertEquals(a.hashCode(), aEqualWithinThresh.hashCode());

        Truth aNotWithinThresh = new DefaultTruth(1.0f, 0.5f - DefaultTruth.DEFAULT_TRUTH_EPSILON * 1.0f);
        assertNotEquals(a, aNotWithinThresh);
        assertNotEquals(a.hashCode(), aNotWithinThresh.hashCode());
    }


//    @Test public void testEpsilon() {
//        float e = 0.1f;
//
//        Truth a = BasicTruth.get(1.0f, 0.9f, e);
//        assertEquals(a.getEpsilon(), e, 0.0001);
//
//        Truth aCopy = BasicTruth.get(1.0f, 0.9f, e);
//        assertEquals(a, aCopy);
//
//        Truth aEqualWithinThresh = BasicTruth.get(1.0f - a.getEpsilon() / 2, 0.9f, e);
//        assertEquals(a, aEqualWithinThresh);
//
//        Truth aNotWithinThresh = BasicTruth.get(1.0f - a.getEpsilon(), 0.9f, e);
//        assertNotEquals(a, aNotWithinThresh);
//    }

    @Test public void testTruthHash() {
        assertEquals( new DefaultTruth(0.5f, 0.5f).hashCode(), new DefaultTruth(0.5f, 0.5f).hashCode() );
        assertNotEquals( new DefaultTruth(1.0f, 0.5f).hashCode(), new DefaultTruth(0.5f, 0.5f).hashCode() );
        assertNotEquals( new DefaultTruth(0.51f, 0.5f).hashCode(), new DefaultTruth(0.5f, 0.5f).hashCode() );
        assertEquals( new DefaultTruth(0.504f, 0.5f).hashCode(), new DefaultTruth(0.5f, 0.5f).hashCode() );
        assertNotEquals( new DefaultTruth(0.506f, 0.5f).hashCode(), new DefaultTruth(0.5f, 0.5f).hashCode() );


        assertEquals( new DefaultTruth(0, 0).hashCode(), new DefaultTruth(0, 0).hashCode() );
        assertEquals( new DefaultTruth(0.004f, 0).hashCode(), new DefaultTruth(0, 0).hashCode() );
        assertNotEquals( new DefaultTruth(0.006f, 0).hashCode(), new DefaultTruth(0, 0).hashCode() );

    }
}
