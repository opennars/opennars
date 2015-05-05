package nars.nal;

import nars.NARSeed;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * NAL tests implemented in Java
 */
@Ignore
@RunWith(Parameterized.class)
public class JavaNALTest extends AbstractNALTest {

    public JavaNALTest(NARSeed b) {
        super(b);
    }

//
//    public void finish(Description test, String status, long nanos) {
//        if (nar.time() == 0) {
//            throw new RuntimeException("Nothing happened (time=0)");
//        }
//
//        String label = test.toString();
//        log.println(label + " " + status + " " +
//                ( (double)nanos)*1E6 + "ms" );
//
//        if (derivations!=null)
//            derivations.print(log);
//    }
//
//
//    @Rule
//    public Stopwatch stopwatch = new Stopwatch() {
//        @Override
//        protected void succeeded(long nanos, Description description) {
//            finish(description, "success", nanos);
//        }
//
//        @Override
//        protected void failed(long nanos, Throwable e, Description description) {
//            finish(description, "fail", nanos);
//        }
//
//        @Override
//        protected void skipped(long nanos, AssumptionViolatedException e, Description description) {
//            finish(description, "skip", nanos);
//        }
//
//        @Override
//        protected void finished(long nanos, Description description) {
//            //finish(description, "finish", nanos);
//        }
//    };
}
