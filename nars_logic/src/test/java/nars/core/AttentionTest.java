/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;


import com.google.common.collect.Iterables;
import nars.NAR;
import nars.prototype.Default;
import nars.nal.concept.Concept;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO test this for each different kind of attention/bag etc
 */
public class AttentionTest {
    
    @Test public void testSampleNextConcept() {
        
        int numConcepts = 32;
        NAR n = new NAR(new Default());
        for (int i = 0; i < numConcepts; i++)
            n.input("<x" + i + " <-> x" + (i + 1) + ">.");
        
        n.run(100);
        
        int c = Iterables.size(n.memory.concepts);
        assertTrue(c > 16);
        
        Set<Concept> uniqueconcepts = new HashSet();
        
        for (int i = 0; i < numConcepts; i++) {
            Concept s = n.memory.conceptNext();
            uniqueconcepts.add(s);
        }

        assertTrue(uniqueconcepts.size() > 1);
        
        int c2 = Iterables.size(n.memory.concepts);
        assertEquals("does not affect # of concepts", c, c2);
    }
    
}
