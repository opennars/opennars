package nars.nal;

import junit.framework.TestCase;
import nars.Global;
import nars.NAR;
import nars.NARSeed;
import nars.analyze.NALysis;
import nars.io.JSONOutput;
import nars.io.in.LibraryInput;
import nars.meter.CountIOEvents;
import nars.meter.TestNAR;
import nars.meter.condition.CountDerivationCondition;
import nars.meter.condition.OutputCondition;
import nars.task.Task;
import nars.util.meter.Metrics;
import nars.util.meter.event.DoubleMeter;
import nars.util.meter.event.HitMeter;
import nars.util.meter.event.ObjectMeter;
import org.junit.After;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by me on 2/10/15.
 */
@Ignore
@RunWith(Parameterized.class)
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
        results = new Metrics().addViaReflection(
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


    public static CountIOEvents eventCounter;
    public static CountDerivationCondition deriveMethodCounter;


    public final TestNAR n;
    public NARSeed build;



    public AbstractNALTest(NARSeed b) {
        super();

        this.build = b;


        Global.DEBUG = true;

        results.clear();

        this.n = new TestNAR(build);
        n.memory.reset(randomSeed);

        eventCounter = null; //new CountOutputEvents(nar, results);
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
        if (analyzeStack && eventCounter!=null) {
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
        testConcepts.hit(nar.memory.getCycleProcess().size());

        results.update(label);


        /*if (derivations!=null)
            derivations.print(log);*/


        if (analyzeStack) {
            eventCounter.reset();
            eventCounter.off();
            deriveMethodCounter.off();
        }


    }

    @Deprecated public static long runScript(TestNAR nar, String path, int maxCycles) {

        script = LibraryInput.getExample(path);

        if (NALysis.showInput)
            System.out.println(script);

        nar.requires.addAll(OutputCondition.getConditions(nar, script, similarsToSave, conditionsCache));


        nar.inputTest(script);


        long start = System.nanoTime();

        nar.run(maxCycles);

        return System.nanoTime() - start;
    }


    public static class Report implements Serializable {

        public final long time;
        public final HitMeter[] eventMeters;
        protected boolean success = true;
        protected Object error = null;
        protected Task[] inputs;
        protected List<OutputCondition> cond = Global.newArrayList();
        transient final int stackElements = 4;

        public Report(TestNAR n) {
            this.time = n.time();

            this.inputs = n.inputs.toArray(new Task[n.inputs.size()]);
            this.eventMeters = n.eventMeters.values().toArray(new HitMeter[0]);
        }

        public void setError(Exception e) {
            if (e!=null) {
                this.error = new Object[]{e.toString(), Arrays.copyOf(e.getStackTrace(), stackElements)};
                success = false;
            }
        }


        public void add(OutputCondition o) {
            cond.add(o);
            if (!o.isTrue()) success = false;
        }

        public boolean isSuccess() {
            return success;
        }

    }


    //Default test procedure
    @After public void test() {

        assertTrue("No conditions tested", !n.requires.isEmpty());

        assertTrue("No cycles elapsed", n.memory.timeSinceLastCycle() > 0);


        Report r = new Report(n);

        r.setError(n.getError());

        for (OutputCondition e : n.requires) {
            r.add(e);
        }

        String s;
        if (!r.isSuccess())
            s = JSONOutput.stringFromFieldsPretty(r);
        else
            s = "";

        assertTrue(s, r.isSuccess());

        n.reset();
    }







}
