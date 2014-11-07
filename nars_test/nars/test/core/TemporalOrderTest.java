/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.test.core;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.test.core.NALTest.ExpectContains;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class TemporalOrderTest {
    
    @Test 
    public void testFutureQuestion() {
        NAR n = new DefaultNARBuilder().build();
        n.addInput("<a --> b>? :/:");
        n.addInput("<c --> d>. :|:");
        ExpectContains futureQuestion = new ExpectContains(n, "<a --> b>? :/:", false);
        assertTrue(!futureQuestion.success());
        n.finish(1);
        
        assertTrue(futureQuestion.success());
        
        n.finish(10);
        
        n.addInput("<c --> d>? :\\:");
        ExpectContains pastQuestion = new ExpectContains(n, "<c --> d>. :\\:", false);
        
        n.finish(10);
        
        assertTrue(pastQuestion.success());
    }
}
