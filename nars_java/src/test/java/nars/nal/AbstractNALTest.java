package nars.nal;

import junit.framework.TestCase;
import nars.analyze.NALysis;
import nars.analyze.meter.CountDerivationCondition;
import nars.analyze.meter.CountOutputEvents;
import nars.Memory;
import nars.NAR;
import nars.ProtoNAR;
import nars.Global;
import nars.io.ExampleFileInput;
import nars.io.condition.OutputCondition;
import nars.io.meter.Metrics;
import nars.io.meter.event.DoubleMeter;
import nars.io.meter.event.HitMeter;
import nars.io.meter.event.ObjectMeter;
import nars.io.test.TestNAR;
import org.junit.After;
import org.junit.Ignore;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by me on 2/10/15.
 */
@Ignore
abstract public class AbstractNALTest extends TestCase {

    public static final long randomSeed = 1;

    private static final int similarsToSave = 3;

    static final DoubleMeter testCost, testTime, testSeed;
    static final HitMeter testConcepts;
    static final ObjectMeter<String> testBuild;
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

    public static final Metrics<String,Object> results = new Metrics().addMeters(
            testCost = new DoubleMeter("Cost"),
            testBuild = new ObjectMeter<String>("Build"),
            testSeed = new DoubleMeter("Seed"),
            testTime = new DoubleMeter("uSecPerCycle"),
            testConcepts = new HitMeter("Concepts")
    );


    public static final CountOutputEvents eventCounter = new CountOutputEvents(results);
    public static final CountDerivationCondition deriveMethodCounter = new CountDerivationCondition(results);


    public final TestNAR nar;
    public final ProtoNAR build;



    public AbstractNALTest(ProtoNAR b) {
        super();

        Memory.resetStatic(randomSeed);
        Global.DEBUG = true;

        this.build = b;
        this.nar = new TestNAR(b);

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
            nar.on(deriveMethodCounter);
        }


    }

    public static void endAnalysis(String label, TestNAR nar, ProtoNAR build, long nanos, long seed, boolean success) {

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
            eventCounter.cancel();
            deriveMethodCounter.cancel();
        }


        //nar.reset(); //to help GC

    }

    public static long runScript(TestNAR nar, String path, int maxCycles) {

        Global.DEBUG = true;

        script = ExampleFileInput.getExample(path);

        if (NALysis.showInput)
            System.out.println(script);

        nar.requires.addAll(OutputCondition.getConditions(nar, script, similarsToSave));

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
    }







}
