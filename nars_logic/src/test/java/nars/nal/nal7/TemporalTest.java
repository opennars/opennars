package nars.nal.nal7;

import nars.NAR;
import nars.nar.Terminal;
import nars.task.Task;
import org.junit.Assert;
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

    @Test public void parsedCorrectOccurrenceTime() {
        NAR n = new Terminal();
        Task t = n.inputTask("<a --> b>. :\\:");
        Assert.assertEquals(0, t.getCreationTime());
        Assert.assertEquals(-(n.memory.duration()), t.getOccurrenceTime());
    }



}
