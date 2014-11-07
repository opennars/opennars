package nars.test.core;

import java.util.List;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.Default;
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

        //testing 2 = floor(dur5/2)
        Interval i2 = Interval.intervalTime(2, dur5);
        assertEquals(1, i2.magnitude);
        assertTrue(i2.name().toString().equals("+2"));
        assertEquals(3, i2.getTime(dur5));
        
        ////testing 2 = floor(dur5/2)
        Interval i3 = Interval.intervalTime(3, dur5);
        assertEquals(1, i3.magnitude);
        assertTrue(i3.name().toString().equals("+2"));
        assertEquals(3, i3.getTime(dur5));
        
        Interval i5 = Interval.intervalTime(5, dur5);
        assertEquals(2, i5.magnitude);
        assertTrue(i5.name().toString().equals("+3"));
        assertEquals(6, i5.getTime(dur5));
        
        /*for (int i = 0; i < 100; i++) {
            System.out.println(i + " " + Interval.intervalTime(i, dur5));
        }*/
        
    }
    
    @Test
    public void testIntervalSequence() {
    
        NAR n = new Default().build();
        Memory m = n.memory;
        
        List<Interval> a11 = Interval.intervalTimeSequence(1, 1, m);
        assertEquals(1, a11.size());
        assertEquals(Interval.intervalTime(1, m), a11.get(0));
        assertEquals(Interval.intervalMagnitude(0), a11.get(0));

        List<Interval> a12 = Interval.intervalTimeSequence(1, 2, m);
        assertEquals(a11, a12);


        {
            //half duration = magnitude 1 ("+2")
            long halfDuration = (n.param).duration.get()/2;
            
            List<Interval> ad1 = Interval.intervalTimeSequence(halfDuration, 1, m);
            assertEquals(1, ad1.size());
            assertEquals(Interval.intervalMagnitude(1), ad1.get(0));
            assertEquals(halfDuration+1, Interval.intervalSequenceTime(ad1, m));

            //unused extra term because time period was exactly reached
            List<Interval> ad2 = Interval.intervalTimeSequence(halfDuration, 2, m);
            assertEquals(2, ad2.size());
            assertEquals(halfDuration, Interval.intervalSequenceTime(ad2, m));
            
        }
        {
            //test ability to represent a range of time periods precisely with up to N terms
            long duration = (n.param).duration.get();
            int numTerms = 6;
            for (int t = 1; t < duration * duration * duration; t++) {
                List<Interval> ad1 = Interval.intervalTimeSequence(t, numTerms, m);
                /*Interval approx = Interval.intervalTime(t, m);                
                System.out.println(t + " = " + ad1 + "; ~= " + 
                        approx + " (error=" + (approx.getTime(m) - t) + ")");*/
                
                assertEquals(t, Interval.intervalSequenceTime(ad1, m));
            }
        }
        
        
        
    }
    
}
