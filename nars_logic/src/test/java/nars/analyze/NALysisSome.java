package nars.analyze;

import nars.Events;
import nars.Global;
import nars.NARSeed;
import nars.meter.TestNAR;
import nars.nar.Default;
import nars.task.Task;
import nars.util.event.Reaction;
import org.junit.Ignore;

import java.io.FileNotFoundException;

/**
 * report filtered by failures
 */
@Ignore
public class NALysisSome extends NALysis {



    public NALysisSome(NARSeed b) {
        super(b);
    }


    public static void main(String[] args) throws FileNotFoundException {

        Global.DEBUG = true;
        Global.DEBUG_DERIVATION_STACKTRACES = true;
        Global.DEBUG_TASK_HISTORY = true;
        showInput = true;
        showOutput = true;
        showTrace = true;

        //String test = "./nal/test8/nal8.1.16.nal";
        //String test = "./nal/test/nal_multistep_sequence.nal";
        //String test = "./nal/test8/nal8.1.7.nal";
        //String test = "./nal/test5/depr/nal5.19.nal";
        //String test = "./nal/test4/depr/nal4.recursion.small.nal";
        //String test = "./nal/test4/nal4.everyday_reasoning.nal";
        //String test = "./nal/test8/nal8.1.0.nal";
        //String test = "./nal/test8/nal8.1.4.nal";
        //String test = "./nal/test8/nal8.1.21.nal";
        String test = "./nal/test2/nal2.10.nal";
        //String test = "./nal/test5/nal5.18.1.nal";
        //String test = "./nal/test5/nal5.18.1.nal";
        //String test = "./nal/test7/nal7.2.nal";
        //String test = "./nal/test7/nal7.18.nal";

        NARSeed build = new Default().setInternalExperience(null);


        //NewNAR build = new Solid(1, 256, 0, 9, 0, 3);
        //NewNAR build = new Default();

        TestNAR n = analyze(
                build,
                test,
                100,
                1
        );
        n.on(new Reaction<Class>() {
            @Override public void event(Class event, Object[] args) {
                Task t = (Task)args[0];
                //System.out.println("Derived: " + t + " " + t.getStamp() + " "  + t.getHistory());
            }
        }, Events.TaskDerive.class);
        n.on(new Reaction<Class>() {
            @Override public void event(Class event, Object[] args) {
                Task t = (Task)args[0];
                //System.out.println("Remove: " + t + " " + t.getStamp() + " " + t.getHistory());
            }
        }, Events.TaskRemove.class);

        n.run();


        //results.printARFF(new PrintStream(dataOut));
        //results.printCSV(new PrintStream(System.out));

        /*n.concept("(&&,<robin --> swimmer>,<robin --> [flying]>)").print(System.out);
        n.concept("<robin --> swimmer>").print(System.out);
        n.concept("<robin --> [flying]>").print(System.out);*/
    }


}
