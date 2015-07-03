/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.nal7;

import nars.NARSeed;
import nars.nal.JavaNALTest;
import nars.nar.Curve;
import nars.nar.Default;
import nars.meter.condition.OutputContainsCondition;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class TemporalOrderTest extends JavaNALTest {

    public TemporalOrderTest(NARSeed b) {
        super(b);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new Default().setInternalExperience(null)},
                {new Default().setInternalExperience(null).level(7)},
                {new Curve()}
        });
    }

    @Test
    public void testFutureQuestion() {


        //TextOutput.out(nar);

        nar.input("<e --> f>. :/:");
        nar.input("<c --> d>. :|:");
        nar.input("<a --> b>. :\\:");
        nar.requires.add(new OutputContainsCondition.InputContainsCondition(nar, "<e --> f>. :/: %1.00;0.90%"));
        nar.requires.add(new OutputContainsCondition.InputContainsCondition(nar, "<c --> d>. :|: %1.00;0.90%"));
        nar.requires.add(new OutputContainsCondition.InputContainsCondition(nar, "<a --> b>. :\\: %1.00;0.90%"));


        //assertTrue(!futureQuestion.isTrue());

        nar.runUntil(1);
        
        //assertTrue(futureQuestion.isTrue());
        
        nar.runUntil(10);

        /*
        try {
            n.addInput("<c --> d>? :\\:");
            assertTrue("Catch invalid input", false);
        }
        catch (RuntimeException e) {
            assertTrue(e.toString().contains("require eternal tense"));
        }
                */

        nar.input("<c --> d>?");


        nar.runUntil(20);
        
    }


}
