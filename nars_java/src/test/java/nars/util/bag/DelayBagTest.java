/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util.bag;

import nars.build.Neuromorphic;
import nars.control.experimental.AntCore;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.narsese.InvalidInputException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class DelayBagTest {

    private NAR n;

    static int numConcepts(NAR n) {
        return ((AntCore)n.memory.concepts).concepts.size();
    }

    @Before
    public void start() {
        Parameters.THREADS = 1;
        Memory.resetStatic(1);
    }

    @Test
    public void testAnt1() { n = new NAR(new Neuromorphic(1));    }
    @Test
    public void testAnt2() { n = new NAR(new Neuromorphic(2));    }
    @Test
    public void testAnt4() { n = new NAR(new Neuromorphic(4));    }

    @After
    public void test() throws InvalidInputException {

        assertTrue(n.memory.concepts != null);
        
        n.input("<a --> b>.");
        
        n.run(1);        
        assertEquals(3, numConcepts(n) );

        n.run(1);        
        assertEquals(3, numConcepts(n) );
        
        n.run(1);        
        assertEquals(3, numConcepts(n) );
        
        n.input("<c --> d>.");
        
        n.run(2);        
        assertEquals(6, numConcepts(n) );

        n.run(30);
        assertEquals(6, numConcepts(n) );
        
        ((AntCore)n.memory.concepts).concepts.remove(
                n.term("<a --> b>"));
        
        assertEquals(5, numConcepts(n) );
        n.run(10);
        assertEquals(5, numConcepts(n) );

        n.input("<a --> b>.");
        n.run(5);
        assertEquals(6, numConcepts(n) );
    }

}
