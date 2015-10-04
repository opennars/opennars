package nars.nal.nal7;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 6/5/15.
 */
public class TemporalTest {

    @Test
    public void testAfter() {

        assertTrue("after", Temporal.after(1, 4, 1));

        assertFalse("concurrent (equivalent)", Temporal.after(4, 4, 1));
        assertFalse("before", Temporal.after(6, 4, 1));
        assertFalse("concurrent (by duration range)", Temporal.after(3, 4, 3));

    }

}
