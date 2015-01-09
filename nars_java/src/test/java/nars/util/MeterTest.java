package nars.util;

import nars.core.NAR;
import nars.core.build.Default;
import nars.io.meter.depr.Tracker;
import nars.io.meter.depr.sensor.CompositeIncidentTracker;
import nars.io.meter.depr.sensor.CompositeSpanTracker;
import nars.io.meter.depr.sensor.DefaultEventMeter;
import nars.io.meter.depr.sensor.EventValueSensor;
import nars.io.meter.depr.sensor.HitPeriodTracker;
import nars.io.meter.resource.MemoryUseTracker;
import nars.io.meter.resource.ThreadCPUTimeTracker;
import nars.io.meter.depr.util.Range;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */


public class MeterTest {
 
//    @Test
//    public void testSpanMeters() throws Exception {
//        
//        CompositeSpanTracker cst = new CompositeSpanTracker(
//                new NanoTimeDurationTracker("NanoTimeDurationExample", 
//                    new Range(0, 10), new Range(10, 50), new Range(50, 150)),
//                
//                new HitPeriodTracker("HitFrequencyExample"),
//                
//                new ThreadCPUTimeTracker("ThreadCPUExample"),
//                
//                new MemoryUseTracker("MemoryUseExample")
//        );
//        
//        for (int i= 0; i < 2; i++) {
//            cst.track();
//            {
//                NAR n = new Default().build();
//                //Thread.sleep((int)(Math.random()*90));
//            }
//            cst.commit();            
//        }
//        for (Tracker x : cst.trackers) {
//            //System.err.println(x);        
//            //System.out.println(" " + x.getSession().collectData());
//        }
//        assertTrue(true);
//        
//    }
//    
//
//    @Test
//    public void testIncidentMeters() {
//        CompositeIncidentTracker cst = new CompositeIncidentTracker(
//                new DefaultEventMeter("incidence1"),
//                new DefaultEventMeter("incidence2")
//        );
//        
//        for (int i= 0; i < 5; i++) {            
//            {
//                NAR n = new Default().build();
//                //Thread.sleep((int)(Math.random()*90));
//            }
//            cst.incident();            
//        }
//        for (Tracker x : cst.trackers) {
//            //System.err.println(x);        
//            //System.out.println(" " + x.getSession().collectData());
//        }        
//        assertTrue(true);
//    }
//    
//    @Test
//    public void testManualMeters() {
//        
//        EventValueSensor dmt = new EventValueSensor("incidence1");
//        
//        for (int i= 0; i < 5; i++) {
//            dmt.setValue(Math.random());
//            dmt.commit();
//        }
//        
//        //System.err.println(dmt);
//        assertTrue(true);
//    }
//    
//    public static void main(String[] args) throws Exception {
//        new MeterTest().testSpanMeters();
//        new MeterTest().testIncidentMeters();
//        new MeterTest().testManualMeters();
//
//    }
}
