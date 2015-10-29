package nars.nal.nal7;

import nars.NAR;
import nars.nar.Default;
import nars.nar.Terminal;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;


/** various tests of the Sequence and Parallel term types */
public class SequenceParallelTest {

    static final Terminal t = new Terminal(); //DURATION=5 by default
    static final int DURATION = t.memory.duration();

    @Test public void testSequenceReduction1() {
        assertEqualTerms("(&/, x, /0)", "x" );
    }
    @Test public void testParalllelReduction1() {
        assertEqualTerms("(&|, x, /0)", "x");
    }

    @Test public void testParallelMaxCycles() {
        assertEqualTerms("(&|, x, /3, /5)", "(&|, x, /5)");
    }

    @Test public void testSequenceReductionComplex() {
        assertEqualTerms(
            "<(&/, <$1 --> (/, open, _, door)>) </> <$1 --> (/, enter, _, room)>>",
            "<<$1 --> (/, open, _, door)> </> <$1 --> (/, enter, _, room)>>");
    }

    @Test public void testSequenceReductionComplex2() {
        //should not be reduced
        assertEqualTerms(
                "<(&/, <($1, key) --> hold>, /5) =/> <($1, room) --> enter>>",
                "<(&/, <($1, key) --> hold>, /5) =/> <($1, room) --> enter>>");
    }



    @Test public void testParallelNegligibleCycles() {
        //since the supplied extra interval duration is less
        //than the effective duration of '(&/,x,/1)',
        //anything less than /5 (the system duration) is
        //meaningless
        assertEqualTerms("(&|, (&/, x, /1), /1)", "(&/, x, /1)");
    }

    @Test public void testSequenceReduction2() {
        assertEquals( "b", t.term("(&/, /0, b)").toString() );
    }
    @Test public void testSequenceReduction3() {
        assertEquals( "b", t.term("(&/, /0, b, /0)").toString() );
    }

    @Test public void testEmbeddedSequenceAndTotalDuration() {

        String es = "(&/, b, /10, c)";
        Sequence e = t.term(es);
        //System.out.println(es + "\n" + e);

        int esDuration = (10 + 2 * DURATION);
        assertEquals( esDuration, e.duration() );
        assertEquals( es, e.toString() );

        String ts = "(&/, a, " + es + ", /1, d)";
        Sequence s = t.term(ts);
        assertEquals( esDuration + 1 + 2 * DURATION, s.duration() );
        assertEquals( ts, s.toString() );

    }

    @Test public void testEmbeddedParallel() {

        String fs = "(&|, b, c, /10)";
        Parallel f = t.term(fs);

        String es = "(&|, b, /10, c)";
        Parallel e = t.term(es);

        assertEquals(e, f); //commutative
        assertEquals(fs, e.toString()); //interval at end

        assertEquals(10, e.duration());


        String ts = "(&|, a, " + fs + ")";
        Parallel s = t.term(ts);

        assertEquals(ts, s.toString());
        assertEquals(10, s.duration()); //maximum contained duration = 10

    }

    @Test public void testSequenceBytes() {
        //TODO
    }
    @Test public void testParallelBytes() {
        //TODO
    }

    @Test public void testSemiDuplicateParallels() {
        //TODO decide if this is correct handling
        String ts = "(&&, (&|, x, /3), (&|, x, /1))";
        Compound c = t.term(ts);


        assertEquals(1, c.length());
        assertEquals(Parallel.class, c.getClass());
        assertEquals(2, ((Parallel)c).duration()); //interpolated duration
    }
    @Test public void testSemiDuplicateSequences() {
        //TODO decide if this is correct handling

        String ts = "(&&, (&/, x, /3), (&/, x, /1))";
        Compound c = t.term(ts);

        assertEquals(1, c.length());
        assertEquals(Sequence.class, c.getClass());
        assertEquals(2, ((Sequence)c).duration()); //interpolated duration
    }

    @Test public void testEmbeddedParallelInSequence() {
    }
    @Test public void testEmbeddedSequenceInParallel() {
    }



    @Test public void testSequenceOfCycleIntervals() {
        assertEqualTerms("(&/, x, /1, /2)", "(&/, x, /3)");
        assertEqualTerms("(&/, /1, /2, x)", "(&/, /3, x)");
        assertEqualTerms("(&/, /1, /2, x, /3, /4)", "(&/, /3, x, /7)");
    }

    private static void assertEqualTerms(String abnormal, String normalized) {
        Term ta = t.term(abnormal);
        Term tb = t.term(normalized);
        assertEquals(ta, tb);
        assertEquals(ta.toString(), tb.toString());
        assertEquals(normalized, tb.toString());
        assertArrayEquals(ta.bytes(), tb.bytes());
    }


    @Test public void testChangingDuration() {
        //TODO test: tasks formed by a NAR with a duration that is being changed reflect these changes

    }
    
    @Test public void testDefaultParallelDuration() {
        //these all should have the same duration (DEFAULT)
        String par1 = "(&|, <a-->b> )";
        String par2 = "(&|, <a-->b>, <c --> d> )";
        String par3 = "(&|, <a-->b>, <c --> d>, /1 )"; // "/1" has no influence here because the terms each have DURATION=5

        assertEquals( DURATION, ((Parallel)t.term(par1)).duration() );
        assertEquals( ((Parallel)t.term(par1)).duration(),  ((Parallel)t.term(par2)).duration());
        assertEquals( ((Parallel)t.term(par2)).duration(),  ((Parallel)t.term(par3)).duration());
    }
    
    @Test public void testParallel() {
        String seq = "(&|, <a-->b>, <a-->b>, <b-->c> )";

        Parallel x = t.term(seq);

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
        assertEquals(3, s.duration());

        assertEquals("output matches input", seq, ss);

    }

    @Test public void testSingleTermSequence() {
        NAR nar = new Default();
        Term x = nar.term("(&/, a)");
        assertNotNull(x);
        assertEquals(Atom.class, x.getClass());
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
    @Test public void testTrailingSuffix() {
        //trailing suffix allowed if the top-level term
        assertEqualTerms("(&/, x, /1)", "(&/, x, /1)");

        //trailing suffix interval removed if not top-level term
        assertEqualTerms("<(&/, x, /1) =/> y>", "<x =/> y>");

        //leading prefix interval remains
        assertEqualTerms("<(&/, /1, x, /2) =/> y>", "<(&/, /1, x) =/> y>");
    }

//    @Test public void testSequenceSentenceNormalization() {
//        //sequences at the top level as terms must not have any trailing intervals
//        NAR nar = new Default();
//
//        String tt = "(&/, a, /1, b, /2)";
//        Sequence term = nar.term(tt);
//        assertNotNull(term);
//
//        //trailng suffix that should be removed when it becomes the sentence's content term
//        Task task = nar.task(tt + ".");
//        assertNotNull(task);
//        assertNotNull(task.getTerm());
//        assertEquals(Sequence.class, task.getTerm().getClass());
//        Sequence ts = (Sequence)task.getTerm();
//        assertEquals(2, ts.length());
//        assertEquals("(&/, a, /1, b)", task.getTerm().toString());
//
//        //no trailing suffix, unchanged
//        Task u = nar.task("(&/, a, /1, b).");
//        assertEquals(Sequence.class, u.getTerm().getClass());
//        Sequence tu = (Sequence)u.getTerm();
//        assertEquals(2, tu.length());
//        assertEquals("(&/, a, /1, b)", u.getTerm().toString());
//
//        //TODO test for the sentence's term to be a different instance if it was modified
//    }

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