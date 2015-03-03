package nars.analyze;

import nars.build.Default;
import nars.core.Events;
import nars.core.NewNAR;
import nars.event.Reaction;
import nars.io.test.TestNAR;
import nars.logic.entity.Task;
import org.junit.Ignore;

import java.io.FileNotFoundException;

/**
 * report filtered by failures
 */
@Ignore
public class NALysisSome extends NALysis {



    public NALysisSome(NewNAR b) {
        super(b);
    }


    public static void main(String[] args) throws FileNotFoundException {

        showInput = true;
        showOutput = true;
        showTrace = false;

        //String test = "./nal/test6/nal6.22.nal";
        //String test = "./nal/test7/nal7.6.nal";
        //String test = "./nal/test8/nal8.1.0.nal";
        //String test = "./nal/test8/nal8.1.21.nal";
        //String test = "./nal/test6/nal6.22.nal";
        //String test = "./nal/test5/nal5.18.1.nal";
        //String test = "./nal/test5/nal5.18.1.nal";
        String test = "./nal/test3/nal3.7.nal";

        //NewNAR build = new Default().setInternalExperience(null);
        NewNAR build = new Default();

        TestNAR n = analyze(
                build,
                test,
                4800,
                1
        );
        n.on(Events.TaskDerive.class, new Reaction() {
            @Override public void event(Class event, Object[] args) {
                Task t = (Task)args[0];
                System.out.println("Derived: " + t);
            }
        });
        n.on(Events.TaskRemove.class, new Reaction() {
            @Override public void event(Class event, Object[] args) {
                Task t = (Task)args[0];
                System.out.println("Remove: " + t + " " + t.getReason());
            }
        });

        n.run();


        //results.printARFF(new PrintStream(dataOut));
        //results.printCSV(new PrintStream(System.out));

    }


}
