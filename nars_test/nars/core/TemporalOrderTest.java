/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import nars.NAR;
import nars.config.Parameters;
import nars.config.Default;
import nars.io.TextOutput;
import nars.lab.testutils.OutputContainsCondition;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class TemporalOrderTest {
    
    @Test 
    public void testFutureQuestion() {
        Parameters.DEBUG = true;
        NAR n = new NAR(new Default());
        new TextOutput(n, System.out);
        
        n.addInput("<e --> f>. :/:");
        n.addInput("<c --> d>. :|:");
        OutputContainsCondition futureQuestion = new OutputContainsCondition(n, "<e --> f>. :/:", 5);
        assertTrue(!futureQuestion.isTrue());
        n.run(1);
        
        assertTrue(futureQuestion.isTrue());
        
        n.run(10);

        /*
        try {
            n.addInput("<c --> d>? :\\:");
            assertTrue("Catch invalid input", false);
        }
        catch (RuntimeException e) {
            assertTrue(e.toString().contains("require eternal tense"));
        }
                */
        
        n.addInput("<c --> d>?");
        
        OutputContainsCondition pastQuestion = new OutputContainsCondition(n, "<c --> d>. :\\:", 5);
        
        n.run(10);
        
        assertTrue(pastQuestion.isTrue());
    }
}
