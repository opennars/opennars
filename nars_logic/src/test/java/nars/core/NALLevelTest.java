package nars.core;


import nars.Global;
import nars.NAR;
import nars.io.out.TextOutput;
import nars.nar.Default;
import nars.meter.condition.OutputCount;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NALLevelTest {



    @Test
    public void testLevel1vs8() {
        Global.DEBUG = true;

        NAR nDefault = new NAR(new Default());
        assertEquals(Global.DEFAULT_NAL_LEVEL, nDefault.nal());

        NAR n1 = new NAR(new Default().level(1));
        OutputCount n1Count = new OutputCount(n1);
        assertEquals(1, n1.nal());

        NAR n8 = new NAR(new Default().level(8));
        OutputCount n8Count = new OutputCount(n8);

        TextOutput.out(n8);

        String productSentence = "<(*,a,b) --> c>.\n<c <-> a>?\n";

        n1.input(productSentence);
        n1.runWhileNewInput(5);

        n8.input(productSentence);
        n8.runWhileNewInput(5);

        assertTrue("NAL8 will accept sentence containing a Product", n8Count.getOutputs() >= 1);
        assertEquals("NAL1 will NOT accept sentence containing a Product", 0, n1Count.getOutputs() + n1Count.getOthers());



    }
}
