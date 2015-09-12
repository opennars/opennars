/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;


import com.google.common.collect.Iterables;
import nars.concept.Concept;
import nars.nar.Default;
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
        Default n = new Default();
        for (int i = 0; i < numConcepts; i++)
            n.believe("<x" + i + " <-> x" + (i + 1) + ">");
        
        //n.runWhileInputting(100);
        n.frame(16);
        
        int c = Iterables.size(n.concepts());
        assertTrue(c > 16);

        n.trace(System.out);

        Set<Concept> uniqueconcepts = new HashSet();
        
        for (int i = 0; i < numConcepts; i++) {
            Concept s = n.getCycleProcess().next();
            uniqueconcepts.add(s);
        }

        assertTrue(uniqueconcepts.size() > 1);
        
        int c2 = Iterables.size(n.getCycleProcess().concepts());
        assertEquals("does not affect # of concepts", c, c2);
    }
    
}
