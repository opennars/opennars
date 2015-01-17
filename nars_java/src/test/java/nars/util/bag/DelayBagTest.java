/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util.bag;

import nars.core.Memory;
import nars.core.NAR;
import nars.build.Neuromorphic;
import nars.control.experimental.AntCore;
import nars.io.narsese.InvalidInputException;
import nars.io.narsese.Narsese;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class DelayBagTest {
    
    static int numConcepts(NAR n) {
        return ((AntCore)n.memory.concepts).concepts.size();
    }
        
    @Test 
    public void testIO() throws InvalidInputException {

        test(new NAR(new Neuromorphic(1)));
        test(new NAR(new Neuromorphic(2)));
        test(new NAR(new Neuromorphic(4)));
    }
    
    public void test(NAR n) throws InvalidInputException {

        Memory.resetStatic(1);

        assertTrue(n.memory.concepts != null);
        
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
        
        ((AntCore)n.memory.concepts).concepts.take(
                new Narsese(n).parseTerm("<a --> b>") );
        
        assertEquals(5, numConcepts(n) );
        n.run(10);
        assertEquals(5, numConcepts(n) );

        n.addInput("<a --> b>.");
        n.run(5);
        assertEquals(6, numConcepts(n) );
    }
}
