package nars.test.util;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.util.meter.Tracker;
import nars.util.meter.track.CompositeSpanTracker;
import nars.util.meter.track.HitFrequencyTracker;
import nars.util.meter.track.MemoryUseTracker;
import nars.util.meter.track.NanoTimeDurationTracker;
import nars.util.meter.track.ThreadCPUTimeTracker;
import nars.util.meter.util.Range;
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
        
        for (int i= 0; i < 5; i++) {
            cst.track();
            {
                NAR n = new DefaultNARBuilder().build();
                //Thread.sleep((int)(Math.random()*90));
            }
            cst.commit();
        }
        
        for (Tracker x : cst.trackers) {
            System.out.println(x);        
            System.out.println(" " + x.getSession().collectData());
        }
    }
    
    @Test
    public void testIncidentMeters() {
        
    }
}
