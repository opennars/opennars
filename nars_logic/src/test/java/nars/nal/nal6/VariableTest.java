/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.nal6;

import nars.Events.Answer;
import nars.NARSeed;
import nars.event.NARReaction;
import nars.io.out.TextOutput;
import nars.meter.condition.OutputContainsCondition;
import nars.nal.JavaNALTest;
import nars.nar.Default;
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

                /*{new Neuromorphic(1)},
                {new Neuromorphic(4)}*/
        });
    }
    
    @Test public void testDepQueryVariableDistinct() {



        /*
            A "Solved" solution of: <(&/,<a --> 3>,+3) =/> <a --> 4>>. %1.00;0.31%
            shouldn't happen because it should not unify #wat with 4 because its not a query variable      
        */        
        new NARReaction(n, true, Answer.class) {
            @Override public void event(Class event, Object[] args) {
                //nothing should cause this event
                assertTrue(Arrays.toString(args), false);
            }
        };
        
        n.requires.add(new OutputContainsCondition(n, "=/> <a --> 4>>.", 5));


        n.input(
                "<a --> 3>. :\\: \n" +
                        "<a --> 4>. :/: \n" +
                        "<(&/,<a --> 3>,?what) =/> <a --> #wat>>?");

        n.run(32);

    }
    

    void unaffected(String left, String right) {

        TextOutput.out(n);

        n.mustInput(1, "<" + left + " ==> " + right + ">.");
        n.input("<" + left + " ==> " + right + ">.");

        n.run(4);
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
