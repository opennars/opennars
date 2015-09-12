/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.nal6;

import nars.NAR;
import nars.io.out.TextOutput;
import nars.io.qa.AnswerReaction;
import nars.nal.JavaNALTest;
import nars.nar.Default;
import nars.task.Task;
import org.junit.Ignore;
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
public class VariableUnificationTest extends JavaNALTest {


    public VariableUnificationTest(NAR b) {
        super(b);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default() },
                //{new Default()},
                //{new DefaultBuffered()},
                //{new DefaultBuffered().setInternalExperience(null)},

                /*{new Neuromorphic(1)},
                {new Neuromorphic(4)}*/
        });
    }
    
    @Test public void testDepQueryVariableDistinct() {


         /*
            A "Solved" solution of: <(&/,<a --> 3>,+3) =/> <a --> 4>>. %1.00;0.31%
            shouldn't happen because it should not unify #wat with 4 because its not a query variable
        */
        new AnswerReaction(nar) {

            @Override
            public void onSolution(Task belief) {
                //nothing should cause this event
                assertTrue(belief.toString(), false);
            }
        };


        assertTrue("test impl unfinished: ", false);
        //tester.requires.add(new OutputContainsCondition(tester.nar, "=/> <a --> 4>>.", 5));


        tester.nar.input(
                "<a --> 3>. :\\: \n" +
                        "<a --> 4>. :/: \n" +
                        "<(&/,<a --> 3>,?what) =/> <a --> #wat>>?");

        tester.run(32);

    }
    

    void unaffected(String left, String right) {

        TextOutput.out(tester.nar);

        tester.mustInput(1, "<" + left + " ==> " + right + ">.");
        tester.nar.input("<" + left + " ==> " + right + ">.");

        tester.run(4);
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

    @Test @Ignore
    public void testNormalizeSomeVars1ac() {
        unaffected(normA, normC);
    }



}
