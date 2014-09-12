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
        
        assertTrue(dur5.getLog() == Math.log(5));
        assertTrue(dur5.get() == 5);
        
        Interval i1 = Interval.intervalTime(1, dur5);
        assertTrue(i1.magnitude == 0);
        assertTrue(i1.name().toString().equals("+1"));
        assertTrue(i1.getTime(dur5) == 1);
        
        Interval i2 = Interval.intervalTime(2, dur5);
        assertTrue(i2.magnitude == 0);
        assertTrue(i2.name().toString().equals("+1"));
        assertEquals(i2.getTime(dur5), 1); //the best the precision can do
        
        Interval i5 = Interval.intervalTime(5, dur5);
        assertTrue(i5.magnitude == 1);
        assertTrue(i5.name().toString().equals("+2"));
        assertTrue(i5.getTime(dur5) == 5);
        
        /*for (int i = 0; i < 100; i++) {
            System.out.println(i + " " + Interval.intervalTime(i, dur5));
        }*/
        
    }
}
