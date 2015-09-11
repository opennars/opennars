package nars.nal.nal7;

import nars.NAR;
import nars.nar.Default;
import org.junit.Test;

/**
 * Created by me on 6/8/15.
 */
public class TemporalInductionTest {

    @Test
    public void testTemporalInduction() {

        String task = "<a --> b>. :|:";
        String task2 = "<c --> d>. :|:";

        NAR n = new Default();

        //TextOutput.out(n);

        n.input(task);
        n.frame(10);
        n.input(task2);

        n.frame(10);

    }
}
