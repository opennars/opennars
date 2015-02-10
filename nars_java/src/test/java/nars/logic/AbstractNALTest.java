package nars.logic;

import junit.framework.TestCase;
import nars.analyze.meter.CountDerivationCondition;
import nars.analyze.meter.CountOutputEvents;
import nars.core.Memory;
import nars.core.NewNAR;
import nars.core.Parameters;
import nars.io.condition.OutputCondition;
import nars.io.meter.Metrics;
import nars.io.meter.event.DoubleMeter;
import nars.io.meter.event.HitMeter;
import nars.io.meter.event.ObjectMeter;
import nars.logic.meta.Derivations;
import org.junit.*;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by me on 2/10/15.
 */
@Ignore
abstract public class AbstractNALTest extends TestCase {

    public static final long randomSeed = 1;


    static final ObjectMeter<Boolean> testSuccess;
    static final DoubleMeter testScore, testTime;
    static final HitMeter testConcepts;
    static final ObjectMeter<String> testBuild;
    private static OutputStream csvOut = null;
    PrintStream log = System.out;

//    static {
//        try {
//            csvOut = new FileOutputStream("/tmp/out.csv");
//        } catch (FileNotFoundException e) {
//            csvOut = System.out;
//        }
//    }

    public static final Metrics<String,Object> results = new Metrics().addMeters(
            testBuild = new ObjectMeter<String>("Build"),
            testSuccess = new ObjectMeter<Boolean>("Success"),
            testScore = new DoubleMeter("Score"),
            testTime = new DoubleMeter("uSecPerCycle"),
            testConcepts = new HitMeter("Concepts")
    );

    //Derivations derivations = new Derivations(false, false);
    Derivations derivations = null;
    public static final CountOutputEvents eventCounter = new CountOutputEvents(results);
    public static final CountDerivationCondition deriveMethodCounter = new CountDerivationCondition(results);


    public final TestNAR nar;
    public final NewNAR build;
    public final List<OutputCondition> conditions;


    public AbstractNALTest(NewNAR b) {
        super();

        Memory.resetStatic(randomSeed);
        Parameters.DEBUG = true;

        this.build = b;
        this.nar = new TestNAR(b);
        conditions = nar.musts;

    }


    /** called before test runs */
    public void initAnalysis(OutputStream csvStream) {

        /*
        if (this.derivations != null) {
            derivations.record(nar);
        }
        */
        nar.addPlugin(eventCounter);
        nar.addPlugin(deriveMethodCounter);


    }



    //Default test procedure
    @After public void test() {

        //assertTrue("No conditions to test", !conditions.isEmpty());
        if (conditions.isEmpty()) {
            System.err.println("WARNING: No Conditions Added");
            new Exception().printStackTrace();
        }


        assertTrue("No cycles elapsed", nar.time() > 0);

        String report = "";
        boolean suc = true;
        for (OutputCondition e : conditions) {
            if (!e.succeeded) {
                report += e.getFalseReason().toString() + '\n';
                suc = false;
            }
            else {
                report += e.getTrueReasons().toString() + '\n';
            }
        }

        assertTrue(report, suc);
    }


    @AfterClass
    public static void report() {
        if (csvOut!=null)
            results.printCSV(new PrintStream(csvOut));
    }





    public void finish(Description test, String status, long nanos) {
        //String label = test.toString();
        /*log.println(label + " " + status + " " +
                ( (double)nanos)*1E6 + "ms" );*/


        boolean success = status.equals("fail") ? false : true;


        /*
        Path pp = Paths.get(path);
        String label = pp.getName(pp.getNameCount()-1) + "/" + build.toString();
        */
        String label = test.getDisplayName();

        testBuild.set(build.toString());
        testSuccess.set(success);
        testScore.set( success ? 1.0 / (1.0 + OutputCondition.cost(conditions)) : 0 );
        testTime.set( (((double)nanos)/1000.0) / (nar.time()) ); //in microseconds
        testConcepts.hit(nar.memory.concepts.size());

        results.update(label);

        eventCounter.reset();
        eventCounter.cancel();
        deriveMethodCounter.cancel();

        nar.reset(); //to help GC


        /*if (derivations!=null)
            derivations.print(log);*/

    }


    @Rule
    public final Stopwatch stopwatch = new Stopwatch() {
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
