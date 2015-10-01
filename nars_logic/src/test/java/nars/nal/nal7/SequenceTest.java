package nars.nal.nal7;

import nars.NAR;
import nars.nal.nal5.Conjunction;
import nars.nar.Default;
import nars.narsese.NarseseParser;
import nars.task.Task;
import nars.term.Term;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by me on 7/1/15.
 */
public class SequenceTest {

    @Test public void testParallel() {
        String seq = "(&|, <a-->b>, <a-->b>, <b-->c> )";

        Conjunction x = NarseseParser.the().term(seq);

        assertEquals(2, x.length());

    }

    @Test public void testConstuction() {
        NAR nar = new Default();

        String seq = "(&/, /1, a, /2, b)";
        Sequence s = nar.term(seq);
        assertNotNull(s);
        assertNotNull(s.intervals());
        assertEquals("only non-interval terms are allowed as subterms", 2, s.length());

        String ss = s.toString();


        assertEquals(s.length() + 1, s.intervals().length);
        assertEquals("[1, 2, 0]", Arrays.toString(s.intervals()));
        assertEquals(3, s.intervalLength());

        assertEquals("output matches input", seq, ss);

    }

    @Test public void testSingleTermSequence() {
        NAR nar = new Default();
        Term x = nar.term("(&/, a)");
        assertNotNull(x);
        assertEquals(Sequence.class, x.getClass());
    }

    @Test public void testSequenceToString() {
        NAR nar = new Default();

        testSeqTermString(nar, "(&/, a, /1, b)");
        //testSeqTermString(nar, "(&/, a, /2, b, /4)");
        testSeqTermString(nar, "(&/, a, /3, b, /5, c, /10, d)");
    }

    private void testSeqTermString(NAR nar, String s) {
        assertEquals(s, nar.term(s).toString());
    }

    @Test public void testSequenceSentenceNormalization() {
        //sequences at the top level as terms must not have any trailing intervals
        NAR nar = new Default();

        String tt = "(&/, a, /1, b, /2)";
        Sequence term = nar.term(tt);
        assertNotNull(term);

        //trailng suffix that should be removed when it becomes the sentence's content term
        Task task = nar.task(tt + ".");
        assertNotNull(task);
        assertNotNull(task.getTerm());
        assertEquals(Sequence.class, task.getTerm().getClass());
        Sequence ts = (Sequence)task.getTerm();
        assertEquals(2, ts.length());
        assertEquals("(&/, a, /1, b)", task.getTerm().toString());

        //no trailing suffix, unchanged
        Task u = nar.task("(&/, a, /1, b).");
        assertEquals(Sequence.class, u.getTerm().getClass());
        Sequence tu = (Sequence)u.getTerm();
        assertEquals(2, tu.length());
        assertEquals("(&/, a, /1, b)", u.getTerm().toString());

        //TODO test for the sentence's term to be a different instance if it was modified
    }

    @Test public void testConceptToString() {

    }

    @Test public void testDistance1() {
        NAR nar = new Default();

        Sequence a = nar.term("(&/, x, /1, y)");
        Sequence b = nar.term("(&/, x, /2, y)");
        assertEquals(1, a.distance1(b));
        assertEquals(1, b.distance1(a));
        assertEquals(0, a.distance1(a));

        Sequence c = nar.term("(&/, x, /1, y)");
        Sequence d = nar.term("(&/, x, /9, y)");
        assertEquals(Long.MAX_VALUE, c.distance1(d, 2));
        assertEquals(1, c.distance1(b, 2));


    }

}