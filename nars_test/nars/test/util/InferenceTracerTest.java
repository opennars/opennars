package nars.test.util;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.util.InferenceTracer;
import org.junit.Test;

/**
 *
 * @author me
 */
public class InferenceTracerTest {
    
    @Test
    public void testConceptAndTask() {
        NAR n = new DefaultNARBuilder().build();
        
        InferenceTracer tracer = new InferenceTracer();
        n.memory.setRecorder(tracer);
        
        n.addInput("<a --> b>.");
        
        n.finish(1);
        
        //tracer.printTime(System.out);                
        //System.out.println(tracer.concept);
        
        assert(tracer.time.size() >= 3); 
        assert(tracer.time.get(1L).size() >= 0);
        
        //assert(tracer.concept.size() == 3);
    
    }
    
    @Test
    public void test2() {
        NAR n = new DefaultNARBuilder().build();
        
        InferenceTracer tracer = new InferenceTracer();
        n.memory.setRecorder(tracer);
        
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
