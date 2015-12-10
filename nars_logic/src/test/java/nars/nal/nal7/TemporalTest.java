package nars.nal.nal7;

import nars.NAR;
import nars.nar.Default;
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

        assertTrue("after", Tense.after(1, 4, 1));

        assertFalse("concurrent (equivalent)", Tense.after(4, 4, 1));
        assertFalse("before", Tense.after(6, 4, 1));
        assertFalse("concurrent (by duration range)", Tense.after(3, 4, 3));

    }

    @Test public void parsedCorrectOccurrenceTime() {
        NAR n = new Default(); //for cycle/frame clock, not realtime like Terminal
        Task t = n.inputTask("<a --> b>. :\\:");
        Assert.assertEquals(0, t.getCreationTime());
        Assert.assertEquals(-(n.memory.duration()), t.getOccurrenceTime());
    }



}
