/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic.nal7;

import junit.framework.TestCase;
import nars.build.Default;
import nars.core.Build;
import nars.core.Events.Answer;
import nars.core.NAR;
import nars.event.AbstractReaction;
import nars.io.TextOutput;
import nars.io.condition.OutputContainsCondition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 TODO convert this to AbstractNALTest
 */
@RunWith(Parameterized.class)
public class VariableTest extends TestCase {

    private final NAR n;

    public VariableTest(Build b) {
        super();
        this.n = new NAR(b);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default().setInternalExperience(null)},
                {new Default()}
        });
    }
    
    @Test public void testDepQueryVariableDistinct() {

        n.addInput("<a --> 3>. :|:");
        n.addInput("<a --> 4>. :/:");
        n.addInput("<(&/,<a --> 3>,?what) =/> <a --> #wat>>?");
        
        /*
            A "Solved" solution of: <(&/,<a --> 3>,+3) =/> <a --> 4>>. %1.00;0.31%
            shouldn't happen because it should not unify #wat with 4 because its not a query variable      
        */        
        new AbstractReaction(n, true, Answer.class) {
            @Override public void event(Class event, Object[] args) {
                //nothing should arrive via Solved.class channel
                assertTrue(false);
            }
        };
        
        OutputContainsCondition e = new OutputContainsCondition(n, "=/> <a --> 4>>.", 5);
        
        n.run(32);
  
        assertTrue(e.isTrue());
    }
    
    @Test public void testQueryVariableUnification() {
        /*
        <a --> 3>. :|:
        <a --> 4>. :/:
        <(&/,<a --> 3>,?what) =/> <a --> ?wat>>?

        Answer <(&/,<a --> 3>,+3) =/> <a --> 4>>. %1.00;0.31%

        because ?wat can be unified with 4 since ?wat is a query variable
       */

        AtomicBoolean solutionFound = new AtomicBoolean(false);
        new AbstractReaction(n, true, Answer.class) {
            @Override public void event(Class event, Object[] args) {
                solutionFound.set(true);
                n.stop();
            }
        };

        TextOutput.out(n);

        n.addInput("<a --> 3>. :|:");
        n.addInput("<a --> 4>. :/:");
        n.addInput("<(&/,<a --> 3>,?what) =/> <a --> ?wat>>?");
        


        //158
        //1738
        //n.run(200); //sufficient for case without internal experience
        n.run(100);
          
        assertTrue(solutionFound.get());
        
    }
}
