package nars.nal;

import junit.framework.TestCase;
import nars.Global;
import nars.NAR;
import nars.io.JSONOutput;
import nars.meter.TestNAR;
import nars.meter.condition.TaskCondition;
import nars.task.Task;
import nars.util.meter.event.HitMeter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by me on 2/10/15.
 */
@Ignore
@RunWith(Parameterized.class)
abstract public class AbstractNALTest extends TestCase {


    //public static boolean analyzeStack = false;

//    static {
//        try {
//            csvOut = new FileOutputStream("/tmp/out.csv");
//        } catch (FileNotFoundException e) {
//            csvOut = System.out;
//        }
//    }


//    public static Metrics<String,Object> results;
//    static DoubleMeter testCost, testTime, testSeed;
//    static HitMeter testConcepts;
//    static ObjectMeter<String> testBuild;


//    public static void reset() {
//        results = new Metrics().addViaReflection(
//                testCost = new DoubleMeter("Cost"),
//                testBuild = new ObjectMeter<String>("Build"),
//                testSeed = new DoubleMeter("Seed"),
//                testTime = new DoubleMeter("uSecPerCycle"),
//                testConcepts = new HitMeter("Concepts")
//        );
//    }
//
//    static {
//        reset();
//    }


    public TestNAR tester;
    public final NAR nar;

    public AbstractNALTest(NAR b) {
        super();

        this.nar = b;

    }

    @Before
    public void start() {

        Global.DEBUG = true;

        /** TestNAR resets the nar */
        this.tester = new TestNAR(nar);

        System.out.println(nar);
    }
//
//    /** called before test runs */
//    public static void initAnalysis(NAR nar) {
//
//        /*
//        if (this.derivations != null) {
//            derivations.record(nar);
//        }
//        */
//        if (analyzeStack && eventCounter!=null) {
//            eventCounter = new EventCount(nar);
//        }
//
//
//
//    }
//
//    public static void endAnalysis(String label, TestNAR nar, NARSeed build, long nanos, long seed, boolean success) {
//
//        testBuild.set(build.toString());
//        testCost.set(TaskCondition.cost(nar.requires));
//        testSeed.set(seed);
//        testTime.set( (((double)nanos)/1000.0) / (nar.time()) ); //in microseconds
//        testConcepts.hit(nar.nar.concepts().size());
//
//        results.update(label);
//
//
//        /*if (derivations!=null)
//            derivations.print(log);*/
//
//
//        if (analyzeStack) {
//            eventCounter.reset();
//            eventCounter.off();
//        }
//
//
//    }

//    @Deprecated public static long runScript(TestNAR tester, String path, int maxCycles) {
//
//        script = LibraryInput.getExample(path);
//
//        if (NALysis.showInput)
//            System.out.println(script);
//
//        tester.requires.addAll(OutputCondition.getConditions(
//                tester.nar,
//                script, similarsToSave, conditionsCache));
//
//
//        tester.inputTest(script);
//
//
//        long start = System.nanoTime();
//
//        tester.run(maxCycles);
//
//        return System.nanoTime() - start;
//    }


    public static class Report implements Serializable {

        public final long time;
        public final HitMeter[] eventMeters;
        protected Object error = null;
        protected Task[] inputs;
        protected List<TaskCondition> cond = Global.newArrayList();
        transient final int stackElements = 4;

        public Report(TestNAR n) {
            this.time = n.time();

            this.inputs = n.inputs.toArray(new Task[n.inputs.size()]);
            this.eventMeters = n.eventMeters.values().toArray(new HitMeter[0]);
        }

        public void setError(Exception e) {
            if (e!=null) {
                this.error = new Object[]{e.toString(), Arrays.copyOf(e.getStackTrace(), stackElements)};
            }
        }

        public void add(TaskCondition o) {
            cond.add(o);
        }

        public boolean isSuccess() {
            for (TaskCondition t : cond)
                if (!t.isTrue())
                    return false;
            return true;
        }

    }




    //Default test procedure
    @After public void test() {

        assertTrue("No conditions tested", !tester.requires.isEmpty());

        //assertTrue("No cycles elapsed", tester.nar.memory().time/*SinceLastCycle*/() > 0);


        Report report = new Report(tester);

        report.setError(tester.getError());

        tester.requires.forEach(report::add);

        String s;
        if (!report.isSuccess()) {
            s = JSONOutput.stringFromFieldsPretty(report);
            assertTrue(s, false);
        }
        else {
            s = "";
        }

    }







}
