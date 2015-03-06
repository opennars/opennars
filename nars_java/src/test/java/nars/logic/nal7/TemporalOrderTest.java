/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic.nal7;

import nars.build.Curve;
import nars.build.Default;
import nars.core.NewNAR;
import nars.io.TextOutput;
import nars.io.condition.OutputContainsCondition;
import nars.logic.JavaNALTest;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class TemporalOrderTest extends JavaNALTest {

    public TemporalOrderTest(NewNAR b) {
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


        TextOutput.out(nar);

        nar.addInput("<e --> f>. :/:");
        nar.addInput("<c --> d>. :|:");
        nar.requires.add(new OutputContainsCondition(nar, "<e --> f>. :/:", 5));
        nar.requires.add(new OutputContainsCondition(nar, "<c --> d>. :\\:", 5));


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

        nar.addInput("<c --> d>?");


        nar.runUntil(20);
        
    }


}
