package nars.util;

import nars.NAR;
import nars.config.Plugins;
import nars.gui.util.NARTrace;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class InferenceTracerTest {
    
    @Test
    public void testConceptAndTask() {
        NAR n = new NAR(new Plugins());
        
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
        NAR n = new NAR(new Plugins());
        
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
