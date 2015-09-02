/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.bag;

import nars.Global;
import nars.NAR;
import nars.cycle.experimental.AntCore;
import nars.nar.experimental.Neuromorphic;
import nars.narsese.InvalidInputException;
import nars.term.Term;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class DelayBagTest {


    static int numConcepts(NAR n) {
        return ((AntCore) n.memory.getCycleProcess()).concepts.size();
    }

    @Before
    public void start() {
        Global.THREADS = 1;
    }

    @Test
    public void testAnt1() { test(1);    }
    @Test
    public void testAnt2() { test(2);    }
    @Test
    public void testAnt4() { test(4);     }

    public void test(int ants) throws InvalidInputException {

        NAR n = new NAR(new Neuromorphic(ants).setInternalExperience(null));

        assertTrue(n.memory.getCycleProcess() != null);
        
        n.input("<a --> b>.");
        
        n.frame(1);
        assertEquals(3, numConcepts(n) );

        n.frame(1);
        assertTrue(3 <= numConcepts(n));
        
        n.frame(1);
        assertTrue(3 <= numConcepts(n));
        
        n.input("<c --> d>.");
        
        n.frame(2);
        assertEquals(6, numConcepts(n) );

        n.frame(30);
        assertEquals(6, numConcepts(n) );
        
        ((AntCore) n.memory.getCycleProcess()).concepts.remove((Term)n.term("<a --> b>"));
        
        assertEquals(5, numConcepts(n) );
        n.frame(10);
        assertEquals(5, numConcepts(n) );

        n.input("<a --> b>.");
        n.frame(5);
        assertTrue(5 <= numConcepts(n) );
    }

}
