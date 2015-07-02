package nars.core;

import nars.Memory;
import nars.NAR;
import nars.nar.Default;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.Interval;
import nars.nal.nal7.Interval.AtomicDuration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        
        Interval i1 = Interval.interval(1, dur5);
        assertTrue(i1.magnitude == 0);
        assertTrue(i1.toString().equals("+1"));
        assertTrue(i1.cycles(dur5) == 1);

        //testing 2 = floor(dur5/2)
        Interval i2 = Interval.interval(2, dur5);
        assertEquals(1, i2.magnitude);
        assertTrue(i2.toString().equals("+2"));
        assertEquals(3, i2.cycles(dur5));
        
        ////testing 2 = floor(dur5/2)
        Interval i3 = Interval.interval(3, dur5);
        assertEquals(1, i3.magnitude);
        assertTrue(i3.toString().equals("+2"));
        assertEquals(3, i3.cycles(dur5));
        
        Interval i5 = Interval.interval(5, dur5);
        assertEquals(2, i5.magnitude);
        assertTrue(i5.toString().equals("+3"));
        assertEquals(6, i5.cycles(dur5));
        
        /*for (int i = 0; i < 100; i++) {
            System.out.println(i + " " + Interval.intervalTime(i, dur5));
        }*/
        
    }
    
    @Test
    public void testIntervalSequence() {

        NAR n = new NAR(new Default());
        Memory m = n.memory;
        
        List<AbstractInterval> a11 = Interval.intervalSequence(1, 1, m);
        assertEquals(1, a11.size());
        assertEquals(Interval.interval(1, m), a11.get(0));
        assertEquals(Interval.interval(0), a11.get(0));

        List<AbstractInterval> a12 = Interval.intervalSequence(1, 2, m);
        assertEquals(a11, a12);


        {
            //half duration = magnitude 1 ("+2")
            long halfDuration = (n.param).duration.get()/2;
            
            List<AbstractInterval> ad1 = Interval.intervalSequence(halfDuration, 1, m);
            assertEquals(1, ad1.size());
            assertEquals(Interval.interval(1), ad1.get(0));
            assertEquals(halfDuration+1, Interval.intervalSequence(ad1, m));

            //unused extra term because time period was exactly reached
            List<AbstractInterval> ad2 = Interval.intervalSequence(halfDuration, 2, m);
            assertEquals(2, ad2.size());
            assertEquals(halfDuration, Interval.intervalSequence(ad2, m));
            
        }
        {
            //test ability to represent a range of time periods precisely with up to N terms
            long duration = (n.param).duration.get();
            int numTerms = 6;
            for (int t = 1; t < duration * duration * duration; t++) {
                List<AbstractInterval> ad1 = Interval.intervalSequence(t, numTerms, m);
                /*Interval approx = Interval.intervalTime(t, m);                
                System.out.println(t + " = " + ad1 + "; ~= " + 
                        approx + " (error=" + (approx.getTime(m) - t) + ")");*/
                
                assertEquals(t, Interval.intervalSequence(ad1, m));
            }
        }
        
        
        
    }
    
}
