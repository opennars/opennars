/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util;

import java.util.Arrays;
import nars.io.meter.SimpleMeter;
import nars.io.meter.TemporalMetrics;
import nars.io.meter.func.FirstOrderDifference;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class MetricsTest {

    static final SimpleMeter<Integer> timeDoubler = new SimpleMeter<Integer>("x") {

        @Override
        protected Integer getValue(Object when, int index) {
            assertEquals(0, index);
            assertTrue(when instanceof Double);
            return ((Double)when).intValue() * 2;
        }            
    };
        
    @Test public void testTemporalMetrics() {

        
        TemporalMetrics<Integer> tm = new TemporalMetrics<Integer>(3);
        tm.addMeter(timeDoubler);
        
        assertEquals(0, tm.numRows());
        assertEquals("signal columns: time and 'x'", 2, tm.getSignals().size()); 
        
        tm.update(1.0);
        
        assertEquals(1, tm.numRows());
        assertEquals(2, tm.getSignalData(1)[0]);
        
        tm.update(1.5);
        tm.update(2.0);
        
        assertEquals(3, tm.numRows());
        
        tm.update(2.5);
        
        assertEquals(3, tm.numRows());
    }

    @Test public void testMeterDerivative() {
        
        TemporalMetrics<Integer> tm = new TemporalMetrics<Integer>(3);
        tm.addMeter(timeDoubler);
        tm.addMeter(new FirstOrderDifference(tm, 1));
        
        assertEquals(3, tm.getSignals().size()); 
        
        tm.update(0.0);       
        tm.update(1.0);
        
        //check the '1' column ('x')
        assertEquals(0, tm.getSignalData(1)[0]);
        assertEquals(2, tm.getSignalData(1)[1]);
        
        tm.update(2.0);

        
        
        //check the '2' column (first order diff)
        assertEquals(null, tm.getSignalData(2)[0]);
        assertEquals(2.0, tm.getSignalData(2)[1]);

        
    }
    
}
