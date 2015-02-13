package nars.analyze;


import nars.build.Curve;
import nars.build.Default;
import nars.core.NewNAR;
import nars.io.TextOutput;
import nars.io.condition.OutputCondition;
import nars.logic.AbstractNALTest;
import nars.logic.TestNAR;
import org.junit.Ignore;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;

import static nars.logic.ScriptNALTest.getPaths;

/**
 * Collects detailed telemetry for a test suite
 */
@Ignore
public class NALysis extends AbstractNALTest {

    public NALysis(NewNAR b) {
        super(b);
    }

/*    public NALysis(NewNAR b, String input) {
        super(b, input);
    }
    */

//    @Parameterized.Parameters(name= "{1} {0}")
//    public static Collection configurations() {
//        return getParams(
//                new String[] { "test2", "test3", "test4" },
//                new Default(),
//                new Default().setInternalExperience(null),
//                new Curve() );
//    }

//    public int getMaxCycles() { return 200; }

    public static void run(NewNAR build, String path, int maxCycles, long seed) {

        String testName = path + "_" + build;

        TestNAR n = new TestNAR(build);

        TextOutput.out(n);

        startAnalysis(n);


        long nanos = runScript(n, path, maxCycles, seed);

        //String report = "";
        boolean suc = n.getError()==null;
        for (OutputCondition e : n.musts) {
            if (!e.succeeded) {
                //report += e.getFalseReason().toString() + '\n';
                suc = false;
            }
            else {
                //report += e.getTrueReasons().toString() + '\n';
            }
        }

        String[] p = path.split("/");
        endAnalysis(p[p.length-1], n, build, nanos, seed, suc);

        //results.printCSVLastLine(System.out);

        if (!suc) {
            System.out.println("FAIL: " + testName);

            //assertTrue("time exceeded", time() > finalCycle);
            n.report(System.out, true, true, true);

        }

    }

//    public void finish(Description test, String status, long nanos) {
//
//        boolean success = status.equals("fail") ? false : true;
//
//        String label = test.getDisplayName();
//
//        endAnalysis(label, nar, build, nanos, success);
//
//    }
//
//
//    @Rule
//    public final Stopwatch stopwatch = new Stopwatch() {
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




    public static void runDir(String dirPath, int maxCycles, long seed, NewNAR... builds) {
        Collection<String> paths = getPaths(dirPath);

        for (String p : paths) {
            for (NewNAR b : builds)
                run(b, p, maxCycles, seed);
        }
    }

    public static void nal(String dirPath, String filter, NewNAR build, int maxCycles) {
        Collection<String> paths = getPaths(dirPath);

        for (String p : paths) {
            if (p.contains(filter))
                run(build, p, maxCycles, 1);
        }
    }


    /** runs the standard set of tests */
    public static void nal1(long seed) {
        runDir("test1", 100, seed,
                new Default().setInternalExperience(null),
                new Default().level(1),
                new Curve(),
                new Curve().setInternalExperience(null));
        runDir("test1", 3000, seed,
                new Default()); //HACK: nal1.8 with internal experience enabled takes forever
    }
    public static void nal2() {
        runDir("test2", 150, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(3), //2 needs sets in 3
                new Curve(),
                new Curve().setInternalExperience(null) );
    }
    public static void nal3() {
        runDir("test3", 200, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(3),
                new Curve(),
                new Curve().setInternalExperience(null) );
    }
    public static void nal4() {
        runDir("test4", 500, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(4),
                new Curve(),
                new Curve().setInternalExperience(null) );
    }
    public static void nal5() {
        runDir("test5", 600, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(5),
                new Curve(),
                new Curve().setInternalExperience(null) );
    }
    public static void nal6() {
        runDir("test6", 5000, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(6),
                new Curve(),
                new Curve().setInternalExperience(null) );
    }
    public static void nal7() {
        runDir("test7", 5000, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(7));
    }
    public static void nal8() {
        runDir("test8", 5000, 1,
                new Default(),
                new Default().setInternalExperience(null));
    }

    public static void main(String[] args) throws FileNotFoundException {

        //csvOut = System.out;
        dataOut = new FileOutputStream("/tmp/out.arff");

        nal1(1);
        nal2();
        nal3();
        nal4();
        nal5();
        nal6();
        nal7();
        nal8();

        results.printARFF(new PrintStream(dataOut));


    }

//    @After
//    public void test() {
//        super.test();
//    }
}

