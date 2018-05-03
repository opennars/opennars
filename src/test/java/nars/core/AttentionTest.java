/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;


import nars.main.NAR;
import com.google.common.collect.Iterables;
import java.util.HashSet;
import java.util.Set;
import nars.entity.Concept;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * TODO run this for each different kind of attention/bag etc
 */
public class AttentionTest {
    
    @Test public void testSampleNextConcept() {
        
        int numConcepts = 32;
        NAR n = new NAR();
        for (int i = 0; i < numConcepts; i++)
            n.addInput("<x" + i + " <-> x" + (i+1) + ">.");
        
        n.cycles(100);
        
        int c = Iterables.size(n.memory.concepts);
        assertTrue(c > 32);
        
        Set<Concept> uniqueconcepts = new HashSet();
        
        for (int i = 0; i < numConcepts; i++) {
            Concept s = n.memory.concepts.peekNext();
            uniqueconcepts.add(s);
        }

        assertTrue(uniqueconcepts.size() > 1);
        
        int c2 = Iterables.size(n.memory.concepts);
        assertEquals("does not affect # of concepts", c, c2);
    }
    
}
