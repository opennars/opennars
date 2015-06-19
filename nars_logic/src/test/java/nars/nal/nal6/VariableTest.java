/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.nal6;

import nars.Events.Answer;
import nars.NARSeed;
import nars.event.NARReaction;
import nars.io.out.TextOutput;
import nars.model.impl.Curve;
import nars.model.impl.Default;
import nars.nal.JavaNALTest;
import nars.testing.condition.OutputContainsCondition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 TODO convert this to AbstractNALTest
 */
@RunWith(Parameterized.class)
public class VariableTest extends JavaNALTest {


    public VariableTest(NARSeed b) {
        super(b);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default().setInternalExperience(null)},
                {new Default()},
                //{new DefaultBuffered()},
                //{new DefaultBuffered().setInternalExperience(null)},
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
        new NARReaction(nar, true, Answer.class) {
            @Override public void event(Class event, Object[] args) {
                //nothing should cause this event
                assertTrue(Arrays.toString(args), false);
            }
        };
        
        nar.requires.add(new OutputContainsCondition(nar, "=/> <a --> 4>>.", 5));


        nar.input(
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

//        AtomicBoolean solutionFound = new AtomicBoolean(false);
//        new AbstractReaction(nar, true, Answer.class) {
//            @Override public void event(Class event, Object[] args) {
//                System.out.println(args[0]);
//                solutionFound.set(true);
//                nar.stop();
//            }
//        };

        TextOutput.out(nar);

        nar.input("<a --> 3>. :|:" + '\n' +
                        "<a --> 4>. :/:" + '\n' +
                        "<(&/,<a --> 3>,?what) =/> <a --> ?wat>>?");

        nar.requires.add(new OutputContainsCondition(nar, "<(&/,<a --> 3>,+3) =/> <a --> 4>>.", 1));

        //158
        //1738
        //n.run(200); //sufficient for case without internal experience
        nar.run(1200);
          
        //assertTrue(solutionFound.get());


    }

    void unaffected(String left, String right) {

        TextOutput.out(nar);

        nar.mustInput(1, "<" + left + " ==> " + right + ">.");
        nar.input("<" + left + " ==> " + right + ">.");

        nar.run(4);
    }

    /*
        //should not become
        //<<(*,bird,animal,$1,$2) --> AndShortcut> ==> <$1 --> $3>>>.
    */
    final String normA = "<(*,bird,animal,$1,$2) <-> AndShortcut>";
    final String normB = "<$1 --> $2>";
    final String normC = "<(*,bird,$1,$abc,$2) <-> AndShortcut>";

    @Test public void testNormalizeSomeVars1ab() {
        unaffected(normA, normB);
    }
    @Test public void testNormalizeSomeVars1ba() {
        unaffected(normB, normA);
    }
    @Test public void testNormalizeSomeVars1ac() {
        unaffected(normA, normC);
    }



}
