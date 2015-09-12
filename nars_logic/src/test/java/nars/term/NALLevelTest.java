package nars.term;


import nars.Global;
import nars.NAR;
import nars.meter.EventCount;
import nars.nar.Default;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NALLevelTest {



    @Test
    public void testLevel1vs8() {
        Global.DEBUG = true;

        NAR nDefault = new Default();
        assertEquals(Global.DEFAULT_NAL_LEVEL, nDefault.nal());

        NAR n1 = new Default().nal(1);
        EventCount n1Count = new EventCount(n1);
        assertEquals(1, n1.nal());

        NAR n8 = new Default().nal(8);
        EventCount n8Count = new EventCount(n8);

        String productSentence = "<(*,a,b) --> c>.\n<c <-> a>?\n";

        n1.input(productSentence);
        n1.frame(5);


        n8.input(productSentence);
        n8.frame(5);

        assertEquals(5, n1.time());
        assertEquals("NAL1 will NOT accept sentence containing a Product", 0, n1Count.numInputs());
        assertTrue("NAL8 will accept sentence containing a Product", n8Count.numInputs() >= 1);




    }
}
