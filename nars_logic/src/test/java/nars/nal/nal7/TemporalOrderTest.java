/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.nal7;

import nars.NARSeed;
import nars.io.out.TextOutput;
import nars.meter.condition.OutputContainsCondition;
import nars.nal.JavaNALTest;
import nars.nar.Default;
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
                {new Default().setInternalExperience(null).level(7)}
        });
    }

    @Test
    public void testFutureQuestion() {


        //TextOutput.out(n);

        n.input("<e --> f>. :/:");
        n.input("<c --> d>. :|:");
        n.input("<a --> b>. :\\:");
        n.requires.add(new OutputContainsCondition.InputContainsCondition(n, "<e --> f>. :/: %1.00;0.90%"));
        n.requires.add(new OutputContainsCondition.InputContainsCondition(n, "<c --> d>. :|: %1.00;0.90%"));
        n.requires.add(new OutputContainsCondition.InputContainsCondition(n, "<a --> b>. :\\: %1.00;0.90%"));


        //assertTrue(!futureQuestion.isTrue());

        n.runUntil(1);
        
        //assertTrue(futureQuestion.isTrue());
        
        n.runUntil(10);

        /*
        try {
            n.addInput("<c --> d>? :\\:");
            assertTrue("Catch invalid input", false);
        }
        catch (RuntimeException e) {
            assertTrue(e.toString().contains("require eternal tense"));
        }
                */

        n.input("<c --> d>?");


        n.runUntil(20);
        
    }


}
