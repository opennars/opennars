package nars.analyze;

import nars.build.Default;
import nars.core.Events;
import nars.core.NewNAR;
import nars.event.Reaction;
import nars.logic.TestNAR;
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

        showOutput = true;
        showTrace = false;

        TestNAR n = analyze(
                new Default().setInternalExperience(null).level(6),
                "./nal/test6/nal6.22.nal",
                256,
                1
        );
        n.on(Events.TaskDerive.class, new Reaction() {
            @Override public void event(Class event, Object[] args) {
                Task t = (Task)args[0];
                System.out.println("Derived: " + t);
            }
        });

        n.run();

        //nal("test5", "5.18", new Default().setInternalExperience(null), 256);

        //results.printARFF(new PrintStream(dataOut));
        //results.printCSV(new PrintStream(System.out));

    }


}
