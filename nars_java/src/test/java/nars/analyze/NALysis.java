package nars.analyze;


import nars.build.Default;
import nars.core.NewNAR;
import nars.logic.AbstractNALTest;
import nars.logic.TestNAR;
import org.junit.Ignore;

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

        TestNAR n = new TestNAR(build);

        startAnalysis(n);

        runScript(n, path, maxCycles, seed);

        boolean success = false; //TODO
        long nanos = 0; //TODO

        endAnalysis(path + "_" + build, n, build, nanos, success);

    }

    public static void runDir(NewNAR build, String dirPath, int maxCycles, long seed) {
        Collection<String> paths = getPaths(dirPath);

        for (String p : paths) {
            run(build, p, maxCycles, seed);
        }
    }



    public static void main(String[] args) {

        runDir(new Default(), "test3", 200, 1);

        csvOut = System.out;

        report();

    }

//    @After
//    public void test() {
//        super.test();
//    }
}

