/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic.nal7;

import junit.framework.TestCase;
import nars.build.Curve;
import nars.build.Default;
import nars.build.DefaultBuffered;
import nars.core.NewNAR;
import nars.core.Events.Answer;
import nars.core.NAR;
import nars.event.AbstractReaction;
import nars.io.condition.OutputContainsCondition;
import nars.logic.JavaNALTest;
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
public class VariableTest extends JavaNALTest {


    public VariableTest(NewNAR b) {
        super(b);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default().setInternalExperience(null)},
                {new Default()},
                {new DefaultBuffered()},
                {new DefaultBuffered().setInternalExperience(null)},
                {new Curve().setInternalExperience(null)}

                /*{new Neuromorphic(1)},
                {new Neuromorphic(4)}*/
        });
    }
    
    @Test public void testDepQueryVariableDistinct() {


        /*
            A "Solved" solution of: <(&/,<a --> 3>,+3) =/> <a --> 4>>. %1.00;0.31%
            shouldn't happen because it should not unify #wat with 4 because its not a query variable      
        */        
        new AbstractReaction(nar, true, Answer.class) {
            @Override public void event(Class event, Object[] args) {
                //nothing should cause this event
                assertTrue(Arrays.toString(args), false);
            }
        };
        
        conditions.add( new OutputContainsCondition(nar, "=/> <a --> 4>>.", 5) );


        nar.addInput(
                "<a --> 3>. :\\: \n" +
                "<a --> 4>. :/: \n" +
                "<(&/,<a --> 3>,?what) =/> <a --> #wat>>?");

        nar.run(32);

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
        new AbstractReaction(nar, true, Answer.class) {
            @Override public void event(Class event, Object[] args) {
                solutionFound.set(true);
                nar.stop();
            }
        };

        //TextOutput.out(n);

        nar.addInput(
                "<a --> 3>. :|:" + '\n' +
                "<a --> 4>. :/:" + '\n' +
                "<(&/,<a --> 3>,?what) =/> <a --> ?wat>>?");

        //158
        //1738
        //n.run(200); //sufficient for case without internal experience
        nar.run(1200);
          
        assertTrue(solutionFound.get());
        
    }
}
