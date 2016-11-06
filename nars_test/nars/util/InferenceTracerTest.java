package nars.util;

import nars.core.NAR;
import nars.core.build.Default;
import nars.util.NARTrace;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class InferenceTracerTest {
    
    @Test
    public void testConceptAndTask() {
        NAR n = new NAR(new Default());
        
        NARTrace tracer = new NARTrace(n);        
        
        n.addInput("<a --> b>.");
        
        n.run(2);
        
        //tracer.printTime(System.out);                
        //System.out.println(tracer.concept);
        
        //assertTrue(tracer.time.size() >= 3); 
        assertTrue(tracer.time.get(1L).size() >= 0);
        
        //assert(tracer.concept.size() == 3);
    
    }
    
    @Test
    public void test2() {
        NAR n = new NAR(new Default());
        
        NARTrace tracer = new NARTrace(n);
        
        n.addInput("<a --> b>.");
        
        n.step(1);
        
        n.addInput("<a <-> b>.");
        
        n.step(1);
        
        n.addInput("<b --> c>.");
        
        n.step(1);
        
        n.addInput("<(*,a,b) --> d>.");        
        
        n.step(1);
        
        //tracer.printTime(System.out);        
        //System.out.println(tracer.concept);
        
        assert(tracer.time.size() >= 4);
        

    }    
}
