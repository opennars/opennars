package nars.test.core;

import nars.language.Interval;
import nars.language.Interval.AtomicDuration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */


public class IntervalTest {

    @Test
    public void testInterval() {
        AtomicDuration dur5 = new AtomicDuration(5);
        
        assertTrue(dur5.getSubDurationLog() == Math.log(5/2f));
        assertTrue(dur5.get() == 5);
        
        Interval i1 = Interval.intervalTime(1, dur5);
        assertTrue(i1.magnitude == 0);
        assertTrue(i1.name().toString().equals("+1"));
        assertTrue(i1.getTime(dur5) == 1);
        
        Interval i2 = Interval.intervalTime(2, dur5);
        assertEquals(1, i2.magnitude);
        assertTrue(i2.name().toString().equals("+2"));
        assertEquals(3, i2.getTime(dur5)); //the best the precision can do
        
        Interval i5 = Interval.intervalTime(5, dur5);
        assertEquals(2, i5.magnitude);
        assertTrue(i5.name().toString().equals("+3"));
        assertEquals(6, i5.getTime(dur5));
        
        /*for (int i = 0; i < 100; i++) {
            System.out.println(i + " " + Interval.intervalTime(i, dur5));
        }*/
        
    }
}
