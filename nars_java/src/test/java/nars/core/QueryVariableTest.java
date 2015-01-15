package nars.core;

import nars.core.build.Default;
import nars.io.TextOutput;
import nars.io.TraceWriter;
import nars.io.narsese.Narsese;
import org.junit.Test;

public class QueryVariableTest {

    /** simple test for solutions to query variable questions */
    @Test public void testQueryVariableSolution() throws Narsese.InvalidInputException {

        TestNAR n = new TestNAR(new Default().level(6));

        TextOutput.out(n);
        new TraceWriter(n, System.out);

        n.believe("<a --> b>");
        n.ask("<?x --> b>");
        n.step(2);
        n.mustBelieve(100, "<a --> b>", 1.0f, 0.9f);
        n.run();
    }
}
