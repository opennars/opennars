package nars.test.util;

import nars.core.NAR;
import nars.core.build.Default;
import nars.util.meter.Sensor;
import nars.util.meter.sensor.CompositeIncidentTracker;
import nars.util.meter.sensor.CompositeSpanTracker;
import nars.util.meter.sensor.DefaultEventSensor;
import nars.util.meter.sensor.EventValueSensor;
import nars.util.meter.sensor.HitPeriodTracker;
import nars.util.meter.sensor.MemoryUseTracker;
import nars.util.meter.sensor.NanoTimeDurationTracker;
import nars.util.meter.sensor.ThreadCPUTimeTracker;
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
                
                new HitPeriodTracker("HitFrequencyExample"),
                
                new ThreadCPUTimeTracker("ThreadCPUExample"),
                
                new MemoryUseTracker("MemoryUseExample")
        );
        
        for (int i= 0; i < 2; i++) {
            cst.track();
            {
                NAR n = new Default().build();
                //Thread.sleep((int)(Math.random()*90));
            }
            cst.commit();            
        }
        for (Sensor x : cst.trackers) {
            //System.err.println(x);        
            //System.out.println(" " + x.getSession().collectData());
        }
        assertTrue(true);
        
    }
    

    @Test
    public void testIncidentMeters() {
        CompositeIncidentTracker cst = new CompositeIncidentTracker(
                new DefaultEventSensor("incidence1"),
                new DefaultEventSensor("incidence2")
        );
        
        for (int i= 0; i < 5; i++) {            
            {
                NAR n = new Default().build();
                //Thread.sleep((int)(Math.random()*90));
            }
            cst.incident();            
        }
        for (Sensor x : cst.trackers) {
            //System.err.println(x);        
            //System.out.println(" " + x.getSession().collectData());
        }        
        assertTrue(true);
    }
    
    @Test
    public void testManualMeters() {
        
        EventValueSensor dmt = new EventValueSensor("incidence1");
        
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
