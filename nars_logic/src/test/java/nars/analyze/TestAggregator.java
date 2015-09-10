package nars.analyze;

import nars.NAR;
import nars.NARStream;
import nars.io.out.TextOutput;
import nars.nar.experimental.DefaultAlann;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Created by me on 9/10/15.
 */
public class TestAggregator extends RunListener {


    private final NAR nar;

    public void testRunStarted(Description description) throws Exception {
    }

    public void testRunFinished(Result result) throws Exception {

    }

    public void testStarted(Description description) throws Exception {
    }


    public void testFinished(Description d) throws Exception {

        String si = "<" + getDescriptionTerm(d) + " --> [ok]>.";
        nar.input(si);

        //System.out.println(failure);
        //System.out.println(JSON.stringFrom(failure));
    }
    public String getDescriptionTerm(Description d) {
        String[] meth = d.getMethodName().split("[\\[\\]]");
        String m = String.join(",", meth);

        return "{" + d.getTestClass().getSimpleName() /*.replace(".",",")*/
                + "," + m + "}";
    }

    public void testFailure(Failure failure) throws Exception {
        Description d = failure.getDescription();

        String si = "<" + getDescriptionTerm(d) + " --> [fail]>.";
        nar.input(si);

        //System.out.println(failure);
        //System.out.println(JSON.stringFrom(failure));
    }

    public void testAssumptionFailure(Failure failure) {
    }

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
    public static void main(String args[])  {
        DefaultAlann da = new DefaultAlann(32);
        da.param.realTime();
        NAR nar = new NAR(da);

        //TextOutput.out(nar);

        nar.input("<{nal1,nal2,nal3,nal4} --> nal>.");
        nar.input("<nars --> [nal]>.");

        new Thread( () -> {
            new TestAggregator(nar, "nars.nal.nal1.NAL1Test");
        }).start();


        new NARStream(nar).forEachFrame(() -> {
            //System.out.println(new MemoryBudget(nar));
        });

        TextOutput.out(nar).setOutputPriorityMin(0.7f);
        nar.loop(3);


    }
}
