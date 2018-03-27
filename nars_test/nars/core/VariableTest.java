/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import java.util.concurrent.atomic.AtomicBoolean;
import nars.io.events.Events.Answer;
import nars.main.NAR;
import nars.io.events.EventHandler;
import nars.lab.testutils.OutputContainsCondition;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author me
 */
public class VariableTest {
 
    NAR n = new NAR();
    
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
        new EventHandler(n, true, Answer.class) {            
            @Override public void event(Class event, Object[] args) {
                //nothing should arrive via Solved.class channel
                assertTrue(false);
            }
        };
        
        OutputContainsCondition e = new OutputContainsCondition(n, "=/> <a --> 4>>.", 5);
        
        n.cycles(32);
  
        assertTrue(e.isTrue());
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
        new EventHandler(n, true, Answer.class) {            
            @Override public void event(Class event, Object[] args) {                
                solutionFound.set(true);
                n.stop();
            }
        };

        n.cycles(1024);
          
        assertTrue(solutionFound.get());
        
    }
}
