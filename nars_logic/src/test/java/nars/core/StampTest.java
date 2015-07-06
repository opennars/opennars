package nars.core;

import nars.task.stamp.Stamp;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static nars.task.stamp.Stamp.toSetArray;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author me
 */


public class StampTest {

    public static long[] a(long... x) {
        return x;
    }

    @Test
    public void testOverlap() {


        assertTrue(Stamp.overlapping(a(1, 2), a(2)));
        assertTrue(Stamp.overlapping(a(1), a(1, 2)));
        assertFalse(Stamp.overlapping(a(1), a(2)));
        assertFalse(Stamp.overlapping(a(2), a(1)));
        assertFalse(Stamp.overlapping(a(1, 2), a(3, 4)));
        assertTrue(Stamp.overlapping(a(1, 2), a(2, 3)));
        assertTrue(Stamp.overlapping(a(2, 3), a(1, 2)));
        assertFalse(Stamp.overlapping(a(2, 3), a(1)));

        assertFalse(Stamp.overlapping(a(1), a(2, 3, 4, 5, 6)));
        assertFalse(Stamp.overlapping(a(2, 3, 4, 5, 6), a(1)));


    }

    @Test 
    public void testStampToSetArray() {
        assertTrue(toSetArray(new long[] { 1, 2, 3 }).length == 3);        
        assertTrue(toSetArray(new long[] { 1, 1, 3 }).length == 2);
        assertTrue(toSetArray(new long[] { 1 }).length == 1);
        assertTrue(toSetArray(new long[] {  }).length == 0);
        assertTrue(
                Arrays.hashCode(toSetArray(new long[] { 3,2,1 }))
                ==
                Arrays.hashCode(toSetArray(new long[] { 2,3,1 }))
        );
        assertTrue(
                Arrays.hashCode(toSetArray(new long[] { 1,2,3 }))
                !=
                Arrays.hashCode(toSetArray(new long[] { 1,1,3 }))
        );    
    }
}
