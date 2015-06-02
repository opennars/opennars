package nars.nal;

import junit.framework.TestCase;
import nars.analyze.NALysis;
import nars.analyze.meter.CountDerivationCondition;
import nars.analyze.meter.CountOutputEvents;
import nars.NAR;
import nars.NARSeed;
import nars.Global;
import nars.io.in.LibraryInput;
import nars.testing.condition.OutputCondition;
import nars.util.meter.Metrics;
import nars.util.meter.event.DoubleMeter;
import nars.util.meter.event.HitMeter;
import nars.util.meter.event.ObjectMeter;
import nars.testing.TestNAR;
import org.junit.After;
import org.junit.Ignore;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by me on 2/10/15.
 */
@Ignore
abstract public class AbstractNALTest extends TestCase {

    public static final long randomSeed = 1;

    private static final int similarsToSave = 3;

    final static Map<String,Task> conditionsCache = new ConcurrentHashMap<>();


    public static OutputStream dataOut = null;
    private static String script;
    PrintStream log = System.out;
    public static boolean analyzeStack = false;

//    static {
//        try {
//            csvOut = new FileOutputStream("/tmp/out.csv");
//        } catch (FileNotFoundException e) {
//            csvOut = System.out;
//        }
//    }


    public static Metrics<String,Object> results;
    static DoubleMeter testCost, testTime, testSeed;
    static HitMeter testConcepts;
    static ObjectMeter<String> testBuild;

    public static void reset() {
        results = new Metrics().addMeters(
                testCost = new DoubleMeter("Cost"),
                testBuild = new ObjectMeter<String>("Build"),
                testSeed = new DoubleMeter("Seed"),
                testTime = new DoubleMeter("uSecPerCycle"),
                testConcepts = new HitMeter("Concepts")
        );
    }

    static {
        reset();
    }


    public static CountOutputEvents eventCounter;
    public static CountDerivationCondition deriveMethodCounter;


    public final TestNAR nar;
    public final NARSeed build;



    public AbstractNALTest(NARSeed b) {
        super();


        Global.DEBUG = true;

        this.build = b;
        this.nar = new TestNAR(b);
        nar.memory.randomSeed(randomSeed);
        results.clear();
        this.eventCounter = new CountOutputEvents(nar, results);

        deriveMethodCounter = null;
        //this.deriveMethodCounter = new CountDerivationCondition(nar, results);

    }


    /** called before test runs */
    public static void initAnalysis(NAR nar) {

        /*
        if (this.derivations != null) {
            derivations.record(nar);
        }
        */
        if (analyzeStack) {
            nar.on(eventCounter);
            if (deriveMethodCounter!=null)
                nar.on(deriveMethodCounter);
        }


    }

    public static void endAnalysis(String label, TestNAR nar, NARSeed build, long nanos, long seed, boolean success) {

        testBuild.set(build.toString());
        testCost.set(OutputCondition.cost(nar.requires));
        testSeed.set(seed);
        testTime.set( (((double)nanos)/1000.0) / (nar.time()) ); //in microseconds
        testConcepts.hit(nar.memory.concepts.size());

        results.update(label);


        /*if (derivations!=null)
            derivations.print(log);*/


        if (analyzeStack) {
            eventCounter.reset();
            eventCounter.off();
            deriveMethodCounter.cancel();
        }


        //nar.reset(); //to help GC

    }

    public static long runScript(TestNAR nar, String path, int maxCycles) {

        script = LibraryInput.getExample(path);

        if (NALysis.showInput)
            System.out.println(script);

        nar.requires.addAll(OutputCondition.getConditions(nar, script, similarsToSave, conditionsCache));

        nar.input(script);

        long start = System.nanoTime();

        nar.run(maxCycles);

        return System.nanoTime() - start;
    }


    //Default test procedure
    @After public void test() {

        assertTrue("No conditions tested", !nar.requires.isEmpty());

        assertTrue("No cycles elapsed", nar.time() > 0);

        StringBuilder report = new StringBuilder();
        report.append('@').append(nar.time()).append(":\n");
        boolean suc = nar.getError()==null;
        for (OutputCondition e : nar.requires) {
            if (!e.succeeded) {
                report.append(e.toString()).append('\n');
                report.append(e.getFalseReason()).append('\n');
                suc = false;
            }
            else {
                report.append(e.getTrueReasons().toString()).append("\n\n");
            }
        }
        if (!suc && script!=null) {
            report.insert(0, script);
            report.insert(0, "\n\n");
        }
        report.insert(0, '\n');


        assertTrue(report.toString(), suc);

        nar.reset();
    }







}
