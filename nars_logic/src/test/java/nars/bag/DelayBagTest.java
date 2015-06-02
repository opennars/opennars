/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.bag;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.model.cycle.experimental.AntCore;
import nars.narsese.InvalidInputException;
import nars.nal.term.Term;
import nars.model.impl.Neuromorphic;
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


    static int numConcepts(NAR n) {
        return ((AntCore)n.memory.concepts).concepts.size();
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

        NAR n = new NAR(new Neuromorphic(ants));

        assertTrue(n.memory.concepts != null);
        
        n.input("<a --> b>.");
        
        n.frame(1);
        assertEquals(3, numConcepts(n) );

        n.frame(1);
        assertTrue(3 <= numConcepts(n));
        
        n.frame(1);
        assertTrue(3 <= numConcepts(n));
        
        n.input("<c --> d>.");
        
        n.frame(2);
        assertEquals(7, numConcepts(n) );

        n.frame(30);
        assertEquals(7, numConcepts(n) );
        
        ((AntCore)n.memory.concepts).concepts.remove((Term)n.term("<a --> b>"));
        
        assertEquals(6, numConcepts(n) );
        n.frame(10);
        assertEquals(6, numConcepts(n) );

        n.input("<a --> b>.");
        n.frame(5);
        assertTrue(6 <= numConcepts(n) );
    }

}
