package nars.nal.nal6;

import nars.NAR;
import nars.nal.JavaNALTest;
import nars.nar.Default;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class QueryVariableTest extends JavaNALTest {

    public QueryVariableTest(NAR b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new Default().nal(5)}
                //{new Neuromorphic(4)},
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
        int time3 = 115;

        //TextOutput.out(n);
        //new TraceWriter(n, System.out);

        tester.nar.frame(time1);
        tester.believe("<a --> b>");
        tester.nar.frame(time2);

        //0.9f conf is expected
        tester.mustBelieve(time3, "<a --> b>", 1.0f, 1.0f, 0.85f, 0.95f);
        tester.ask("<?x --> b>");

        tester.run();
    }
}
