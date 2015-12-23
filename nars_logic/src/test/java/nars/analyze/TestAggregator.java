package nars.analyze;

import nars.NAR;
import nars.nar.Default;
import nars.util.meter.MemoryBudget;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by me on 9/10/15.
 */
public class TestAggregator extends RunListener {


    private final NAR nar;

    String testName = null;
    Set<Description> success = new HashSet();

    @Override
    public void testRunStarted(Description description) throws Exception {
        testName = "nartest" + System.currentTimeMillis();
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        for (Description d : success) {
            describe(d, true);
        }
    }

    @Override
    public void testStarted(Description d) throws Exception {

        String si = '<' + getDescriptionTerm(d) + " --> " + testName + ">.";
        nar.input(si);
    }


    @Override
    public void testFinished(Description d) throws Exception {

        success.add(d);

        //System.err.println("finished: " + d.getAnnotations());

        //System.out.println(failure);
        //System.out.println(JSON.stringFrom(failure));
    }

    public String getDescriptionTerm(Description d) {
        String[] meth = d.getMethodName().split("[\\[\\]]");
        String m = String.join(",", meth);

        return '{' + d.getTestClass().getSimpleName() /*.replace(".",",")*/
                + ',' + m + '}';
    }

    protected void describe(Description d, boolean success) {


        String si = '<' + getDescriptionTerm(d) + " --> [" +
                (success ? "ok" : "fail") + "]>.";

        nar.input(si);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {

        Description d = failure.getDescription();

        success.remove(d);

        describe(d, false);

        //System.out.println(failure);
        //System.out.println(JSON.stringFrom(failure));
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
    }

    @Override
    public void testIgnored(Description description) throws Exception {
    }

    public TestAggregator(NAR nar, String... classnames) {

        this.nar = nar;

        JUnitCore core = new JUnitCore();

        core.addListener(this);
        for (String c : classnames) {
            try {
                core.run(Class.forName(c));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }
    public static void main(String[] args)  {
        Default da = new Default(128, 1, 2, 3);
        //da.memory.realTime();
        NAR nar = da;

        //nar.input("<?x --> [fail]>?");
        //nar.input("<?x --> [ok]>?");
        nar.input("<ok <-> fail>. %0%");
        nar.input("<<#x --> [fail]> =/> fix(#x)>.");


//        nar.input("<{nal1,nal2,nal3,nal4} --> nal>.");
//        nar.input("<nars --> [nal]>.");

        new Thread( () -> {
            new TestAggregator(nar, "nars.nal.nal1.NAL1Test");
        }).start();
        new Thread( () -> {
            new TestAggregator(nar, "nars.nal.nal2.NAL2Test");
        }).start();

//        new NARStream(nar).forEachFrame(() -> {
//            //System.out.println(new MemoryBudget(nar));
//        });

        //TextOutput.out(nar).setOutputPriorityMin(0.5f);

        for (int i = 0; i < 100; i++) {
            nar.frame(100);
            System.out.println(new MemoryBudget(nar));
        }


    }
}
