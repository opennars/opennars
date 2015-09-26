package nars.task;

import nars.NAR;
import nars.nar.Default;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 6/8/15.
 */
public class TaskSeedTest {

    @Test public void testTenseEternality() {
        NAR n = new Default();

        String s = "<a --> b>.";

        assertTrue(TaskSeed.make(n.memory, n.term(s)).setEternal().isEternal());

        assertTrue("default is timeless", TaskSeed.make(n.memory, n.term(s)).isTimeless());

        assertTrue("tense=eternal is eternal", TaskSeed.make(n.memory, n.term(s)).setEternal().isEternal());

        assertTrue("present is non-eternal", !TaskSeed.make(n.memory, n.term(s)).present(n.memory).isEternal());

    }

    @Test public void testTenseOccurrenceOverrides() {

        NAR n = new Default();

        String s = "<a --> b>.";

        //the final occurr() or tense() is the value applied
        assertTrue(!TaskSeed.make(n.memory, n.term(s)).setEternal().occurr(100).isEternal());
        assertTrue(!TaskSeed.make(n.memory, n.term(s)).setEternal().present(n.memory).isEternal());
        assertTrue(TaskSeed.make(n.memory, n.term(s)).occurr(100).setEternal().isEternal());
    }


//    @Test public void testStampTenseOccurenceOverrides() {
//
//        NAR n = new NAR(new Default());
//
//        Task parent = n.task("<x --> y>.");
//
//
//        String t = "<a --> b>.";
//
//
//        Stamper st = new Stamper(parent, 10);
//
//        //the final occurr() or tense() is the value applied
//        assertTrue(!n.memory.task(n.term(t)).eternal().stamp(st).isEternal());
//        assertTrue(n.memory.task(n.term(t)).stamp(st).eternal().isEternal());
//        assertEquals(20, n.memory.task(n.term(t)).judgment().parent(parent).stamp(st).occurr(20).get().getOccurrenceTime());
//    }

}
