/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal.nal7;

import nars.NAR;
import nars.nal.JavaNALTest;
import nars.nar.Default;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class TemporalOrderTest extends JavaNALTest {

    public TemporalOrderTest(NAR b) {
        super(b);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new Default()},
                {new Default().nal(7)}
        });
    }

    @Test
    public void testFutureQuestion() {
        assertTrue("test impl unfinished", false);
//
//
//        //TextOutput.out(n);
//
//        tester.nar.input("<e --> f>. :/:");
//        tester.nar.input("<c --> d>. :|:");
//        tester.nar.input("<a --> b>. :\\:");
//        tester.requires.add(new OutputContainsCondition.InputContainsCondition(tester.nar, "<e --> f>. :/: %1.00;0.90%"));
//        tester.requires.add(new OutputContainsCondition.InputContainsCondition(tester.nar, "<c --> d>. :|: %1.00;0.90%"));
//        tester.requires.add(new OutputContainsCondition.InputContainsCondition(tester.nar, "<a --> b>. :\\: %1.00;0.90%"));
//
//
//        //assertTrue(!futureQuestion.isTrue());
//
//        tester.runUntil(1);
//
//        //assertTrue(futureQuestion.isTrue());
//
//        tester.runUntil(10);
//
//        /*
//        try {
//            n.addInput("<c --> d>? :\\:");
//            assertTrue("Catch invalid input", false);
//        }
//        catch (RuntimeException e) {
//            assertTrue(e.toString().contains("require eternal tense"));
//        }
//                */
//
//        tester.nar.input("<c --> d>?");
//
//
//        tester.runUntil(20);
        
    }


}
