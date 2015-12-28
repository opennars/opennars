package nars.task;

import nars.NAR;
import nars.nal.nal7.Tense;
import nars.nar.Default;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 6/8/15.
 */
public class MutableTaskTest {

    @Test public void testTenseEternality() {
        NAR n = new Default();

        String s = "<a --> b>";

        assertTrue(Tense.isEternal(new MutableTask(n.term(s)).eternal().getOccurrenceTime()));

        assertTrue("default is timeless", new MutableTask(n.term(s)).isTimeless());

        assertTrue("tense=eternal is eternal", Tense.isEternal(new MutableTask(n.term(s)).eternal().getOccurrenceTime()));

        assertTrue("present is non-eternal", !Tense.isEternal(new MutableTask(n.term(s)).present(n.memory).getOccurrenceTime()));

    }

    @Test public void testTenseOccurrenceOverrides() {

        NAR n = new Default();

        String s = "<a --> b>";

        //the final occurr() or tense() is the value applied
        assertTrue(!Tense.isEternal(new MutableTask(n.term(s)).eternal().occurr(100).getOccurrenceTime()));
        assertTrue(!Tense.isEternal(new MutableTask(n.term(s)).eternal().present(n.memory).getOccurrenceTime()));
        assertTrue(Tense.isEternal(new MutableTask(n.term(s)).occurr(100).eternal().getOccurrenceTime()));
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
