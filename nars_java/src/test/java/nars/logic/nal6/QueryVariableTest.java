package nars.logic.nal6;

import nars.build.Default;
import nars.build.Neuromorphic;
import nars.core.Build;
import nars.io.narsese.InvalidInputException;
import nars.logic.AbstractNALTest;
import nars.logic.TestNAR;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static nars.logic.nal7.Tense.Eternal;


public class QueryVariableTest extends AbstractNALTest {

    public QueryVariableTest(Build b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new Default().setInternalExperience(null)},
                {new Default().level(5)},
                {new Neuromorphic(4)},
        });
    }


    /** simple test for solutions to query variable questions */
    @Test public void testQueryVariableSolution() throws InvalidInputException {

        /*
        int time1 = 5;
        int time2 = 15;
        int time3 = 5;
        */

        int time1 = 55;
        int time2 = 115;
        int time3 = 55;

        TestNAR n = new TestNAR(new Default().level(6));

        //TextOutput.out(n);
        //new TraceWriter(n, System.out);

        n.step(time1);
        n.believe("<a --> b>");
        n.step(time2);

        //0.9f conf is expected
        n.mustBelieve(time3, "<a --> b>", 1.0f, 1.0f, 0.85f, 0.95f);
        n.ask("<?x --> b>");

        n.run();
    }
}
