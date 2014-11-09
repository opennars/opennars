/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.test.core;

import nars.core.NAR;
import nars.core.Build;
import nars.core.Param;
import nars.core.build.Default;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author me
 */
public class TestParamJSON {
    
    @Test 
    public void testReserializeParam() {
        
        Param p = NAR.build(Default.class).param;
        
        String j = p.toString();
        
        Param r = Param.fromJSON(j);
                
        assertEquals(p.getForgetMode(),r.getForgetMode());
        
        assertEquals(p.duration.get(), r.duration.get());
        assertEquals(p.duration.getSubDurationLog(), r.duration.getSubDurationLog(), 0.01);


        assertEquals(p.taskLinkForgetDurations.get(), r.taskLinkForgetDurations.get(), 0.01);
        
        assertEquals(p.termLinkRecordLength.get(), r.termLinkRecordLength.get(), 0.01);            
        
    }

    
    @Test 
    public void testReserializeGenome() {
        
        Build p = new Default();;
        
        String j = p.toString();
        
        System.out.println(j);
        
    }
    
//    @Test
//    public void testGenome() {
//        Param p = new Default().build().param();
//        double[] d = p.toGenome();
//        
//        System.out.println(Arrays.toString(d));
//        
//        assertTrue(d.length > 1);
//    }
    
}
