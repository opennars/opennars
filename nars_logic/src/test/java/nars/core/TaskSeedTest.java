package nars.core;

import nars.NAR;
import nars.model.impl.Default;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 6/8/15.
 */
public class TaskSeedTest {

    @Test public void testTenseEternality() {
        NAR n = new NAR(new Default());

        String s = "<a --> b>.";

        assertTrue(n.memory.newTask(n.term(s)).eternal().isEternal());

        assertTrue("default is timeless", n.memory.newTask(n.term(s)).isTimeless());

        assertTrue("tense=eternal is eternal", n.memory.newTask(n.term(s)).eternal().isEternal());

        assertTrue("present is non-eternal", !n.memory.newTask(n.term(s)).present().isEternal());

    }

    @Test public void testTenseOccurenceOverrides() {

        NAR n = new NAR(new Default());

        String s = "<a --> b>.";

        //the final occurr() or tense() is the value applied
        assertTrue(!n.memory.newTask(n.term(s)).eternal().occurr(100).isEternal());
        assertTrue(!n.memory.newTask(n.term(s)).eternal().present().isEternal());
        assertTrue(n.memory.newTask(n.term(s)).occurr(100).eternal().isEternal());
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
