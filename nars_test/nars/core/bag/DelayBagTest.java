/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.bag;

import nars.core.NAR;
import nars.core.build.Neuromorphic;
import nars.core.control.experimental.AntAttention;
import nars.io.narsese.Narsese;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author me
 */
public class DelayBagTest {
    
    static int numConcepts(NAR n) {
        return ((AntAttention)n.memory.concepts).concepts.size();
    }
        
    @Test 
    public void testIO() throws Narsese.InvalidInputException {
        test(new NAR(new Neuromorphic(1)));
        test(new NAR(new Neuromorphic(2)));
        test(new NAR(new Neuromorphic(4)));
    }
    
    public void test(NAR n) throws Narsese.InvalidInputException {

        assertTrue(n.memory.concepts!=null);
        
        n.addInput("<a --> b>.");
        
        n.run(1);        
        assertEquals(3, numConcepts(n) );

        n.run(1);        
        assertEquals(3, numConcepts(n) );
        
        n.run(1);        
        assertEquals(3, numConcepts(n) );
        
        n.addInput("<c --> d>.");
        
        n.run(2);        
        assertEquals(6, numConcepts(n) );

        n.run(30);
        assertEquals(6, numConcepts(n) );
        
        ((AntAttention)n.memory.concepts).concepts.take(
                new Narsese(n).parseTerm("<a --> b>") );
        
        assertEquals(5, numConcepts(n) );
        n.run(10);
        assertEquals(5, numConcepts(n) );

        n.addInput("<a --> b>.");
        n.run(5);
        assertEquals(6, numConcepts(n) );
    }
}
