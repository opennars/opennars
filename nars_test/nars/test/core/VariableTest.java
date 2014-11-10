/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.test.core;

import java.util.concurrent.atomic.AtomicBoolean;
import nars.core.Events.Solved;
import nars.core.NAR;
import nars.core.build.Default;
import nars.inference.AbstractObserver;
import nars.test.core.NALTest.ExpectContains;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author me
 */
public class VariableTest {
 
    NAR n = NAR.build(Default.class);
    
    @Before public void init() {
        n.addInput("<a --> 3>. :|:");
        n.addInput("<a --> 4>. :/:");        
    }
    
    @Test public void testDepQueryVariableDistinct() {
                          
        n.addInput("<(&/,<a --> 3>,?what) =/> <a --> #wat>>?");
        
        /*
            A "Solved" solution of: <(&/,<a --> 3>,+3) =/> <a --> 4>>. %1.00;0.31%
            shouldn't happen because it should not unify #wat with 4 because its not a query variable      
        */        
        new AbstractObserver(n, true, Solved.class) {            
            @Override public void event(Class event, Object[] args) {
                //nothing should arrive via Solved.class channel
                assertTrue(false);
            }
        };
        
        ExpectContains e = new ExpectContains(n, "=/> <a --> 4>>.", false);
        
        n.finish(32);
  
        assertTrue(e.success());
    }
    
    @Test public void testQueryVariableUnification() {
        /*
        <a --> 3>. :|:
        <a --> 4>. :/:
        <(&/,<a --> 3>,?what) =/> <a --> ?wat>>?

        Solved <(&/,<a --> 3>,+3) =/> <a --> 4>>. %1.00;0.31%

        because ?wat can be unified with 4 since ?wat is a query variable
       */

        n.addInput("<(&/,<a --> 3>,?what) =/> <a --> ?wat>>?");
        
        AtomicBoolean solutionFound = new AtomicBoolean(false);
        new AbstractObserver(n, true, Solved.class) {            
            @Override public void event(Class event, Object[] args) {                
                solutionFound.set(true);
            }
        };

        n.finish(48);
          
        assertTrue(solutionFound.get());
        
    }
}
