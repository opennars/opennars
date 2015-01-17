package nars.logic;

import junit.framework.TestCase;
import nars.core.Build;
import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.meta.Derivations;
import org.junit.After;
import org.junit.AssumptionViolatedException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.PrintStream;

@Ignore
@RunWith(Parameterized.class)
public class AbstractNALTest extends TestCase {

    public static final long randomSeed = 1;

    //Derivations derivations = new Derivations(false, false);
    Derivations derivations = null;

    public TestNAR n;


    public AbstractNALTest(Build b) {

        this.n = new TestNAR(b);

        if (this.derivations != null) {
            derivations.record(n);
        }

    }


    @After
    public void test() {
        Memory.resetStatic(randomSeed);
        Parameters.DEBUG = true;
        n.run();
    }



    PrintStream log = System.out;


    public void finish(Description test, String status, long nanos) {
        String label = test.toString();
        log.println(label + " " + status + " " +
                ( (double)nanos)*1E6 + "ms" );

        if (derivations!=null)
            derivations.print(log);
    }


    @Rule
    public Stopwatch stopwatch = new Stopwatch() {
        @Override
        protected void succeeded(long nanos, Description description) {
            finish(description, "success", nanos);
        }

        @Override
        protected void failed(long nanos, Throwable e, Description description) {
            finish(description, "fail", nanos);
        }

        @Override
        protected void skipped(long nanos, AssumptionViolatedException e, Description description) {
            finish(description, "skip", nanos);
        }

        @Override
        protected void finished(long nanos, Description description) {
            //finish(description, "finish", nanos);
        }
    };
}
