package nars.logic;

import nars.core.*;
import nars.io.ExampleFileInput;
import nars.io.condition.OutputCondition;
import nars.io.meter.Metrics;
import nars.io.meter.event.DoubleMeter;
import nars.io.meter.event.HitMeter;
import nars.io.meter.event.ObjectMeter;
import org.junit.*;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Ignore
@RunWith(Parameterized.class)
abstract public class AbstractNALScriptTests extends AbstractNALTest {


    public static final ObjectMeter<Boolean> testSuccess;
    public static final DoubleMeter testScore, testTime, testConcepts;

    public static final Metrics<String,Object> results = new Metrics().addMeters(
            testSuccess = new ObjectMeter<Boolean>("Success"),
            testScore = new DoubleMeter("Score"),
            testTime = new DoubleMeter("Time"),
            testConcepts = new DoubleMeter("Concepts")
    );
    private final NAR.PluginState eventCounterState;

    public static class EventCounter extends AbstractPlugin {

//        public static final DoubleMeter numIn = new DoubleMeter("IN");
//        public static final DoubleMeter numOut = new DoubleMeter("OUT");

        Map<Class, HitMeter> eventMeters = new HashMap();

        public EventCounter(Metrics m) {
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
    public static final EventCounter eventCounter = new EventCounter(results);

    private final int similarsToSave = 0;
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
        results.printCSV(System.out);
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

        testSuccess.set(success);
        testScore.set( OutputCondition.cost(conditions));
        testTime.set(nanos);

        results.update(label);

        eventCounter.reset();


        nar.removePlugin(eventCounterState);
        nar.reset(); //to help GC

        /*if (derivations!=null)
            derivations.print(log);*/
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
