package nars.meta;

import nars.NAR;
import nars.io.out.TextOutput;
import nars.nar.NewDefault;
import org.junit.Test;

/**
 * Created by me on 8/15/15.
 */
public class NAL1RuleTest {

    @Test
    public void testNAL1() {
        //Deriver d = Deriver.defaults;

        NAR n = new NAR(new NewDefault());
        n.input("<a --> b>.");
        n.input("<b --> c>.");

        TextOutput.out(n);
        n.frame(50);


    }
}
