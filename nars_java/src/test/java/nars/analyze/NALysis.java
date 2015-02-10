package nars.analyze;


import nars.build.Curve;
import nars.build.Default;
import nars.core.NewNAR;
import nars.io.condition.OutputCondition;
import nars.logic.AbstractNALTest;
import nars.logic.TestNAR;
import org.junit.Ignore;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
        System.out.println("run: " + testName);

        TestNAR n = new TestNAR(build);

        startAnalysis(n);

        long nanos = runScript(n, path, maxCycles, seed);

        //String report = "";
        boolean suc = true;
        for (OutputCondition e : n.musts) {
            if (!e.succeeded) {
                //report += e.getFalseReason().toString() + '\n';
                suc = false;
            }
            else {
                //report += e.getTrueReasons().toString() + '\n';
            }
        }

        endAnalysis(testName, n, build, nanos, suc);

        results.printCSVLastLine(System.out);

    }

    public static void runDir(String dirPath, int maxCycles, long seed, NewNAR... builds) {
        Collection<String> paths = getPaths(dirPath);

        for (String p : paths) {
            for (NewNAR b : builds)
                run(b, p, maxCycles, seed);
        }
    }



    public static void main(String[] args) throws FileNotFoundException {

        runDir("test1", 100, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(1),
                new Curve(),
                new Curve().setInternalExperience(null) );

        runDir("test2", 150, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(2),
                new Curve(),
                new Curve().setInternalExperience(null) );

        runDir("test3", 200, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(3),
                new Curve(),
                new Curve().setInternalExperience(null) );

        runDir("test4", 500, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(4),
                new Curve(),
                new Curve().setInternalExperience(null) );

        runDir("test5", 600, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(5),
                new Curve(),
                new Curve().setInternalExperience(null) );

        runDir("test6", 800, 1,
                new Default(),
                new Default().setInternalExperience(null),
                new Default().level(6),
                new Curve(),
                new Curve().setInternalExperience(null) );

        //csvOut = System.out;
        csvOut = new FileOutputStream("/tmp/out.csv");

        report();

    }

//    @After
//    public void test() {
//        super.test();
//    }
}

