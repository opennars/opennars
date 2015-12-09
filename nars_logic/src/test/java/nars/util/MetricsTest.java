/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util;

import nars.util.meter.FunctionMeter;
import nars.util.meter.TemporalMetrics;
import nars.util.meter.func.BasicStatistics;
import nars.util.meter.func.FirstOrderDifference;
import org.junit.Ignore;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class MetricsTest {

    static final FunctionMeter<Integer> timeDoubler = new FunctionMeter<Integer>("x") {

        @Override
        public Integer getValue(Object when, int index) {
            assertEquals(0, index);
            assertTrue(when instanceof Double);
            return ((Double)when).intValue() * 2;
        }            
    };
        
    @Test public void testTemporalMetrics() {

        
        TemporalMetrics<Integer> tm = new TemporalMetrics<>(3);
        tm.add(timeDoubler);
        
        assertEquals(0, tm.numRows());
        assertEquals("signal columns: time and 'x'", 2, tm.getSignals().size()); 
        
        tm.update(1.0);
        
        assertEquals(1, tm.numRows());
        assertEquals(2, tm.getData(1)[0]);
        
        tm.update(1.5);
        tm.update(2.0);
        
        assertEquals(3, tm.numRows());
        
        tm.update(2.5);
        
        assertEquals(3, tm.numRows());
    }

    @Test public void testMeterDerivative() {
        
        TemporalMetrics<Integer> tm = new TemporalMetrics<>(3);
        tm.add(timeDoubler);
        tm.add(new FirstOrderDifference(tm, timeDoubler.signalID(0)));
        
        assertEquals(3, tm.getSignals().size()); 
        
        tm.update(0.0);       
        tm.update(1.0);
        
        //check the '1' column ('x')
        assertEquals(0, tm.getData(1)[0]);
        assertEquals(2, tm.getData(1)[1]);
        
        tm.update(2.0);

        
        
        //check the '2' column (first order diff)
        assertEquals(null, tm.getData(2)[0]);
        assertEquals(2.0, tm.getData(2)[1]);

        
    }
    
    @Ignore
    @Test public void testSummaryStatistics() {

        TemporalMetrics<Double> tm = new TemporalMetrics<>(10);
        tm.add(new BasicStatistics(tm, tm.getSignalIDs()[0]));
        
        for (int i = 0; i < 10; i++) {
            tm.update(0.1 * i);
        }


        //noinspection OverlyComplexAnonymousInnerClass
        PrintStream sb = new PrintStream(System.out) {
        
            int line = 0;
            
            @Override
            public void println(String x) {
                String eq = null;
                switch (line++) {
                    case 0: eq = "\"key\",\"key.mean\",\"key.stdev\""; break;
                    case 1: eq = "0,0,0"; break;
                    case 3: eq = "0.2,0.1,0.1"; break;
                }
                if (eq!=null) {
                    assertEquals(eq, x);
                }
            }          
        };
        tm.printCSV(sb);
        
    }
}
