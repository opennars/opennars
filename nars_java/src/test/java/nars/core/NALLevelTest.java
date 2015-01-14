package nars.core;


import nars.core.build.Default;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NALLevelTest {

    @Test
    public void testLevel1vs8() {
        Parameters.DEBUG = true;

        NAR nDefault = new NAR(new Default());
        assertEquals(Parameters.DEFAULT_NAL, nDefault.nal());

        NAR n1 = new NAR(new Default().level(1));
        n1.param.exceptionOnExceedingNALLevel.set(true);
        assertEquals(1, n1.nal());

        NAR n8 = new NAR(new Default().level(8));
        n8.param.exceptionOnExceedingNALLevel.set(true);


        String productSentence = "<(*,a,b) --> c>.";
        try {
            n1.addInput(productSentence);
            n1.run(5);
            assertTrue("NAL1 should reject sentence containing a Product", false);
        }
        catch (Exception e) {
            assertTrue(true);
        }

        try {
            n8.addInput(productSentence);
            n1.run(5);
        }
        catch (Exception e) {
            assertTrue("NAL8 will accept sentence containing a Product", false);
        }


    }
}
