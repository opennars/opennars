package nars.task;

import nars.Global;
import nars.NAR;
import nars.NARSeed;
import nars.nar.Default;
import nars.nar.NewDefault;
import nars.nar.experimental.Equalized;
import nars.nar.experimental.Solid;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by me on 8/31/15.
 */
public class UniqueInputSerialTest {

    @Test public void inputTwoUniqueTasks() {
        inputTwoUniqueTasks(new Default());
        inputTwoUniqueTasks(new Solid(4, 1, 1, 1, 1, 1));
        inputTwoUniqueTasks(new Equalized(4, 1, 1));
        inputTwoUniqueTasks(new NewDefault());
    }

    public void inputTwoUniqueTasks(NARSeed build) {

        Global.DEBUG = true;

        NAR n = new NAR(build);

        Task x = n.input(n.task("<a --> b>."));
        assertArrayEquals(new long[] { 1 }, x.getEvidence());
        n.frame();

        Task y = n.input(n.task("<b --> c>."));
        assertArrayEquals(new long[] { 2 }, y.getEvidence());
        n.frame();

        n.reset();

        List<Task> z = n.inputs("<e --> f>.  <g --> h>. "); //test when they are input on the same parse
        assertArrayEquals(new long[] { 3 }, z.get(0).getEvidence());
        assertArrayEquals(new long[] { 4 }, z.get(1).getEvidence());

        n.frame(10);

        Task q = n.input(n.task("<c --> d>."));
        assertArrayEquals(new long[] { 5 }, q.getEvidence());

    }
}
