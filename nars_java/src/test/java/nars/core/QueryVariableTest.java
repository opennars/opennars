package nars.core;

import nars.core.build.Default;
import nars.io.narsese.Narsese;
import org.junit.Test;

public class QueryVariableTest {

    /** simple test for solutions to query variable questions */
    @Test public void testQueryVariableSolution() throws Narsese.InvalidInputException {

        TestNAR n = new TestNAR(new Default().level(6));

        //TextOutput.out(n);
        //new TraceWriter(n, System.out);

        n.step(5);
        n.believe("<a --> b>");
        n.step(15);

        n.mustBelieve(5, "<a --> b>", 1.0f, 0.9f);
        n.ask("<?x --> b>");

        n.run();
    }
}
