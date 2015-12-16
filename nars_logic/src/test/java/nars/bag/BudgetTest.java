package nars.bag;

import nars.util.data.Util;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author me
 */
public class BudgetTest {

    @Test
    public void test() {
        assertEquals(0, Util.bin(0, 10));
        assertEquals(1, Util.bin(0.1f, 10));
        assertEquals(9, Util.bin(0.9f, 10));
        assertEquals(9, Util.bin(0.925f, 10));
        assertEquals(10, Util.bin(0.975f, 10));
        assertEquals(10, Util.bin(1.0f, 10));
        
        
        assertEquals(0, Util.bin(0.0f, 9));
        assertEquals(1, Util.bin(0.1f, 9));
        assertEquals(8, Util.bin(0.9f, 9));
        assertEquals(9, Util.bin(1.0f, 9));
    }

}
