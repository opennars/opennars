/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.io.TextOutput;
import nars.core.NALTest.ExpectContains;
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
        NAR n = new Default().build();
        new TextOutput(n, System.out);
        
        n.addInput("<e --> f>. :/:");
        n.addInput("<c --> d>. :|:");
        ExpectContains futureQuestion = new ExpectContains(n, "<e --> f>. :/:", false);
        assertTrue(!futureQuestion.success());
        n.run(1);
        
        assertTrue(futureQuestion.success());
        
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
        
        ExpectContains pastQuestion = new ExpectContains(n, "<c --> d>. :\\:", false);
        
        n.run(10);
        
        assertTrue(pastQuestion.success());
    }
}
