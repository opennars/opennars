package nars.test.util;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.util.meter.Tracker;
import nars.util.meter.track.CompositeIncidentTracker;
import nars.util.meter.track.CompositeSpanTracker;
import nars.util.meter.track.DefaultIncidentTracker;
import nars.util.meter.track.DefaultManualTracker;
import nars.util.meter.track.HitFrequencyTracker;
import nars.util.meter.track.MemoryUseTracker;
import nars.util.meter.track.NanoTimeDurationTracker;
import nars.util.meter.track.ThreadCPUTimeTracker;
import nars.util.meter.util.Range;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */


public class MeterTest {
 
    @Test
    public void testSpanMeters() throws Exception {
        
        CompositeSpanTracker cst = new CompositeSpanTracker(
                new NanoTimeDurationTracker("NanoTimeDurationExample", 
                    new Range(0, 10), new Range(10, 50), new Range(50, 150)),
                
                new HitFrequencyTracker("HitFrequencyExample"),
                
                new ThreadCPUTimeTracker("ThreadCPUExample"),
                
                new MemoryUseTracker("MemoryUseExample")
        );
        
        for (int i= 0; i < 2; i++) {
            cst.track();
            {
                NAR n = new DefaultNARBuilder().build();
                //Thread.sleep((int)(Math.random()*90));
            }
            cst.commit();            
        }
        for (Tracker x : cst.trackers) {
            //System.err.println(x);        
            //System.out.println(" " + x.getSession().collectData());
        }
        assertTrue(true);
        
    }
    

    @Test
    public void testIncidentMeters() {
        CompositeIncidentTracker cst = new CompositeIncidentTracker(
                new DefaultIncidentTracker("incidence1"),
                new DefaultIncidentTracker("incidence2")
        );
        
        for (int i= 0; i < 5; i++) {            
            {
                NAR n = new DefaultNARBuilder().build();
                //Thread.sleep((int)(Math.random()*90));
            }
            cst.incident();            
        }
        for (Tracker x : cst.trackers) {
            //System.err.println(x);        
            //System.out.println(" " + x.getSession().collectData());
        }        
        assertTrue(true);
    }
    
    @Test
    public void testManualMeters() {
        
        DefaultManualTracker dmt = new DefaultManualTracker("incidence1");
        
        for (int i= 0; i < 5; i++) {
            dmt.setValue(Math.random());
            dmt.commit();
        }
        
        //System.err.println(dmt);
        assertTrue(true);
    }
    
    public static void main(String[] args) throws Exception {
        new MeterTest().testSpanMeters();
        new MeterTest().testIncidentMeters();
        new MeterTest().testManualMeters();

    }
}
