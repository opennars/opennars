package nars.logic;

import nars.core.*;
import nars.io.ExampleFileInput;
import nars.io.condition.OutputCondition;
import nars.io.condition.OutputCount;
import nars.io.meter.Meter;
import nars.io.meter.Metrics;
import nars.io.meter.Signal;
import nars.io.meter.event.DoubleMeter;
import nars.io.meter.event.HitMeter;
import nars.io.meter.event.ObjectMeter;
import nars.logic.entity.Task;
import nars.util.data.CuckooMap;
import org.junit.*;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Ignore
@RunWith(Parameterized.class)
abstract public class AbstractNALScriptTests extends AbstractNALTest {


    static final ObjectMeter<Boolean> testSuccess;
    static final DoubleMeter testScore, testTime;
    static final HitMeter testConcepts;
    static final ObjectMeter<String> testBuild;

    private static OutputStream csvOut;

    static {
        try {
            csvOut = new FileOutputStream("/tmp/out.csv");
        } catch (FileNotFoundException e) {
            csvOut = System.out;
        }
    }

    public static final Metrics<String,Object> results = new Metrics().addMeters(
            testBuild = new ObjectMeter<String>("Build"),
            testSuccess = new ObjectMeter<Boolean>("Success"),
            testScore = new DoubleMeter("Score"),
            testTime = new DoubleMeter("uSecPerCycle"),
            testConcepts = new HitMeter("Concepts")
    );
    private final NAR.PluginState eventCounterState;
    private final NAR.PluginState deriveMethodCounterState;

    public static class CountDerivationCondition extends AbstractPlugin {

        //SM = success method
        final static String meterPrefix = "SM";
        private final Metrics metrics;
        final Map<Task, StackTraceElement[]> stacks = new CuckooMap();
        final List<OutputCondition> successesThisCycle = new ArrayList();

        public CountDerivationCondition(Metrics m) {
            super();
            this.metrics = m;
        }

        @Override
        public Class[] getEvents() {
            return new Class[] { Events.TaskDerive.class, OutputCondition.class, Events.CycleEnd.class };
        }

        @Override public void onEnabled(NAR n) {       }

        @Override public void onDisabled(NAR n) {        }

        @Override
        public void event(Class event, Object[] args) {

            if (event == OutputCondition.class) {

                OutputCondition o = (OutputCondition) args[0];

                if (!o.succeeded) {
                    throw new RuntimeException(o + " signaled when it has not succeeded");
                }

                //buffer to calculate at end of cycle when everything is collected
                successesThisCycle.add(o);

            }
            else if (event == Events.TaskDerive.class) {
                Task t = (Task)args[0];
                stacks.put(t, Thread.currentThread().getStackTrace());
            }
            else if (event == Events.CycleEnd.class) {

                /** usually true reason tasks should only be one, because
                 * this event will be triggered only the first time it has
                 * become successful. */
                for (OutputCondition o : successesThisCycle) {
                    for (Task tt : o.getTrueReasons())
                        traceStack(tt);
                }

                //reset everything for next cycle
                stacks.clear();
                successesThisCycle.clear();
            }
        }

        public void traceStack(Task t) {
            StackTraceElement[] s = stacks.get(t);
            if (s == null) {
                //probably a non-derivation condition, ex: immediate reaction to an input event, etc.. or execution
                //throw new RuntimeException("A stackTrace for successful output condition " + t + " was not recorded");
                return;
            }

            int excludeSuffix = 1;
            String startMethod = "reason";

            boolean tracing = false;
            for (int i = 0; i < s.length; i++) {
                StackTraceElement e = s[i];

                String className = e.getClassName();
                String methodName = e.getMethodName();


                if (tracing && className.contains(".ConceptFireTask") && methodName.equals("accept")) {
                    tracing = false;
                }

                if (tracing) {
                    int cli = className.lastIndexOf(".");
                    if (cli!=-1)
                        className = className.substring(cli, className.length()); //class's simpleName

                    String sm = meterPrefix + className + '_' + methodName;

                    HitMeter m = (HitMeter) metrics.getMeter(sm);
                    if (m == null) {
                        metrics.addMeter(m = new HitMeter(sm));
                    }
                    m.hit();
                }
                else if (className.endsWith(".NAL") && methodName.equals("deriveTask")) {
                    tracing = true; //begins with next stack element
                }
            }
        }
    }

    public static class CountOutputEvents extends AbstractPlugin {

//        public static final DoubleMeter numIn = new DoubleMeter("IN");
//        public static final DoubleMeter numOut = new DoubleMeter("OUT");

        Map<Class, HitMeter> eventMeters = new HashMap();

        public CountOutputEvents(Metrics m) {
            super();

            for (Class c : getEvents()) {
                HitMeter h = new HitMeter(c.getSimpleName());
                eventMeters.put(c, h);
                m.addMeter(h);
            }

        }

        public static final Class[] ev = new Class[] {
                Events.IN.class,
                Events.EXE.class,
                Events.OUT.class,
                Events.ERR.class,
                Events.Answer.class,
        };

        @Override
        public Class[] getEvents() {
            return ev;
        }

        @Override
        public void onEnabled(NAR n) {

        }

        @Override
        public void onDisabled(NAR n) {

        }

        @Override
        public void event(Class event, Object[] args) {
            eventMeters.get(event).hit();
        }

        public void reset() {
            for (HitMeter h : eventMeters.values())
                h.reset();
        }
    }

    public static final CountOutputEvents eventCounter = new CountOutputEvents(results);
    public static final CountDerivationCondition deriveMethodCounter = new CountDerivationCondition(results);

    private final int similarsToSave = 1;
    private final List<OutputCondition> conditions;
    private final String path;
    private final NewNAR build;
    boolean success;

    public AbstractNALScriptTests(NewNAR b, String path) {
        this(b, path, 1);
    }

    public AbstractNALScriptTests(NewNAR b, String path, long rngSeed) {
        super(b);

        this.path = path;
        this.build = b;

        Memory.resetStatic(rngSeed);
        Parameters.DEBUG = true;

        String script = ExampleFileInput.getExample(path);
        this.conditions = OutputCondition.getConditions(nar, script, similarsToSave);

        nar.addInput(script);
        eventCounterState = nar.addPlugin(eventCounter);
        deriveMethodCounterState = nar.addPlugin(deriveMethodCounter);


    }

    abstract public int getMaxCycles();

    @Test
    public void theTest() {

        success = true;

        nar.run(getMaxCycles());


    }

    @After
    public void finish() {    }

    @AfterClass
    public static void report() {
        if (csvOut!=null)
            results.printCSV(new PrintStream(csvOut));
    }



    public void finish(Description test, String status, long nanos) {
        //String label = test.toString();
        /*log.println(label + " " + status + " " +
                ( (double)nanos)*1E6 + "ms" );*/

        
        success = status.equals("fail") ? false : true;
        
        for (OutputCondition e : conditions) {
            if (!e.succeeded) {
                success = false;
                break;
            }
        }

        Path pp = Paths.get(path);
        
        String label = pp.getName(pp.getNameCount()-1) + "/" + build.toString();

        testBuild.set(build.toString());
        testSuccess.set(success);
        testScore.set( success ? 1.0 / (1.0 + OutputCondition.cost(conditions)) : 0 );
        testTime.set( (((double)nanos)/1000.0) / (nar.time()) ); //in microseconds
        testConcepts.hit(nar.memory.concepts.size());

        results.update(label);

        eventCounter.reset();


        nar.removePlugin(eventCounterState);
        nar.removePlugin(deriveMethodCounterState);
        nar.reset(); //to help GC


        /*if (derivations!=null)
            derivations.print(log);*/


        assertTrue(success);
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

    public static Collection getParams(String directories, NewNAR... builds) {
        return getParams(new String[] { directories }, builds);
    }

    //@Parameterized.Parameters(name="{1} {0}")
    public static Collection getParams(String[] directories, NewNAR... builds) {
        Map<String, String> et = ExampleFileInput.getUnitTests(directories);
        Collection<String> t = et.values();

        Collection<Object[]> params = new ArrayList(t.size() * builds.length);
        for (String script : t) {
            for (NewNAR b : builds) {
                params.add(new Object[] { b, script });
            }
        }
        return params;
    }

}
