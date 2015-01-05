package nars.core;

import java.util.Arrays;
import static junit.framework.TestCase.assertTrue;
import static nars.entity.Stamp.toSetArray;
import org.junit.Test;

/**
 *
 * @author me
 */


public class TestStamp {

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
