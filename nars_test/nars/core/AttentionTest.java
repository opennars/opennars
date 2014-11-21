/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;


import com.google.common.collect.Iterables;
import java.util.HashSet;
import java.util.Set;
import nars.core.build.Default;
import nars.entity.Concept;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * TODO run this for each different kind of attention/bag etc
 */
public class AttentionTest {
    
    @Test public void testSampleNextConcept() {
        
        int numConcepts = 32;
        NAR n = NAR.build(Default.class);
        for (int i = 0; i < numConcepts; i++)
            n.addInput("<x" + i + " <-> x" + (i+1) + ">.");
        
        n.finish(100);
        assertTrue(Iterables.size(n.memory.concepts) > 32);
        
        Set<Concept> uniqueconcepts = new HashSet();
        
        for (int i = 0; i < numConcepts; i++) {
            Concept s = n.memory.sampleNextConcept();
            uniqueconcepts.add(s);
        }

        assertTrue(uniqueconcepts.size() > 1);
    
    }
    
}
