package nars.nal.nal7;

import nars.NAR;
import nars.Op;
import nars.nar.Default;
import nars.nar.Terminal;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import org.junit.Test;

import java.util.Arrays;

import static nars.$.$;
import static org.junit.Assert.*;


/** various tests of the Sequence and Parallel term types */
public class SequenceParallelTest {

    static final Terminal t = new Terminal(); //DURATION=5 by default
    static final int DURATION = t.memory.duration();

    @Test public void testSequenceTaskNormalization() {
        Terminal t = new Terminal();

        Task x = t.inputTask("(&/, <a-->b>, /10). :|:");
        assertEquals(t.term("<a-->b>"), x.getTerm());
        assertEquals(-10, x.getOccurrenceTime());

        Task y = t.inputTask("(&/, <a-->b>, /10).");
        assertEquals(t.term("<a-->b>"), y.getTerm());
        assertEquals(Tense.ETERNAL, y.getOccurrenceTime());

        Task z = t.inputTask("(&/, a, /10). :|:");
        assertNull(z);
    }

    @Test public void testSequenceReduction1() {
        assertEqualTerms("(&/, x, /0)", "x" );
    }
    @Test public void testParalllelReduction1() {
        assertEqualTerms("(&|, x, /0)", "x");
    }
    @Test public void testParalllelReduction2() {
        assertEqualTerms("(&|, x, y, /10)", "(&|, x, y)");
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
        //anything less than the supplied eventDuration
        //is meaningless
        assertEqualTerms("(&|, (&/, x, /1), /1)", "(&/, x, /1)");
    }

    @Test public void testSequenceReduction2() {
        assertEquals( "b", t.term("(&/, /0, b)").toString() );
    }
    @Test public void testSequenceReduction3() {
        assertEquals( "b", t.term("(&/, /0, b, /0)").toString() );
    }

    @Test public void testSequenceDuration() {
        assertEquals(DURATION + 1, ((Sequence)t.term("(&/, x, /1)")).duration());
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

        assertEquals( ts, s.toString() );
        assertEquals( esDuration + 1 + 2 * DURATION, s.duration() );

    }

    @Test public void testEmbeddedParallel() {

        String fs = "(&|, b, c)";
        Parallel f = t.term(fs);

        String es = "(&|, c, b)";
        Parallel e = t.term(es);

        assertEquals(e, f); //commutative
        assertEquals(fs, e.toString()); //interval at end

        assertEquals(DURATION, e.duration());


        String ts = "(&|, a, " + fs + ")";
        Parallel s = t.term(ts);

        assertEquals(ts, s.toString());
        assertEquals(DURATION, s.duration()); //maximum contained duration = 10

    }

    @Test public void testSequencesInParallel() {
        String p = "(&|, (&/, a, b), c).";

        //for parallel conjunction, duration is determined by the maximum enduring subterm, ie. the sub-sequence

        Task f = t.inputTask(p);
        assertEquals(DURATION * 2, f.duration());
        assertEquals(DURATION * 2, ((Parallel)f.getTerm()).duration());
    }
    @Test public void testParallelInSequence() {
        Sequence f = t.term("(&/, (&|, (&/, a, /1), c), d )");
        assertEquals(1 + DURATION * 2, f.duration());
    }

//
//    @Test public void testSequenceBytes() {
//        //TODO
//    }
//    @Test public void testParallelBytes() {
//        //TODO
//    }

//    @Test public void testSemiDuplicateParallels() {
//        //TODO decide if this is correct handling
//
//        int dur1 = DURATION + 1;
//        Compound c = t.term("(&&, (&|, x, /" + dur1 + "), (&|, x, /1))");
//
//
//        assertEquals(1, c.length());
//        assertEquals(Parallel.class, c.getClass());
//        assertEquals(dur1 /*?*/, ((Parallel)c).duration()); //interpolated duration
//    }

    @Test public void testParallelWithoutSlashZero() {

        assertEquals(
                    "$0.50;0.80;0.95$ (&|, <hold --> (/, _, John, key)>, <John --> (/, hold, _, key)>). :0: %1.00;0.90%",
        t.inputTask("(&|, <John --> (/, hold, _, key)>, <hold --> (/, _, John, key)>, /0).").toString());
    }

    @Test public void testSemiDuplicateSequences() {
        //TODO decide if this is correct handling

        String ts = "(&&, (&/, x, /3), (&/, x, /1))";
        Compound c = t.term(ts);

        assertEquals(1, c.size());
        assertEquals(Sequence.class, c.getClass());
        assertEquals(DURATION + 3 /*?*/, ((Sequence)c).duration()); //interpolated duration
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
        assertEquals(tb, ta);
        assertEquals(ta.toString(), tb.toString());
        assertEquals(normalized, tb.toString());
        assertArrayEquals(ta.bytes(), tb.bytes());
    }


    @Test public void testChangingDuration() {
        //TODO test: tasks formed by a NAR with a duration that is being changed reflect these changes

    }

    @Test public void testDefaultParallelReduction() {
        //these all should have the same duration (DEFAULT)
        String par1 = "(&|, <a --> b> )";
        assertEquals(Op.INHERITANCE, t.term(par1).op());
    }

    @Test public void testDefaultParallelDuration() {
        //these all should have the same duration (DEFAULT)
        String par1 = "(&|, <a --> b>, <c --> d>, /2 )";
        String par2 = "(&|, <a --> b>, <c --> d> )";
        String par3 = "(&|, <a --> b>, <c --> d>, /1 )"; // "/1" has no influence here because the terms each have DURATION=5

        assertEquals( DURATION, ((Parallel)t.term(par1)).duration() );
        assertEquals( ((Parallel)t.term(par1)).duration(),  ((Parallel)t.term(par2)).duration());
        assertEquals( ((Parallel)t.term(par2)).duration(),  ((Parallel)t.term(par3)).duration());
    }
    
    @Test public void testParallel() {
        String seq = "(&|, <a-->b>, <a-->b>, <b-->c> )";

        Parallel x = t.term(seq);

        assertEquals(2, x.size());
    }

    @Test public void testConstruction() {

        String seq = "(&/, /1, a, /2, b)";
        Sequence s = t.term(seq);
        assertNotNull(s);
        assertNotNull(s.intervals());
        assertEquals("only non-interval terms are allowed as subterms", 2, s.size());

        String ss = s.toString();


        assertEquals(s.size() + 1, s.intervals().length);
        assertEquals("[1, 2, 0]", Arrays.toString(s.intervals()));
        assertEquals(1 + DURATION + 2 + DURATION, s.duration());

        assertEquals("output matches input", seq, ss);

    }

    @Test public void testSingleTermSequence() {
        NAR nar = new Terminal();
        Term x = nar.term("(&/, a)");
        assertNotNull(x);
        assertEquals(Atom.class, x.getClass());
    }

    @Test public void testSequenceToString() {
        NAR nar = new Terminal();

        testSeqTermString(nar, "(&/, a, /1, b)");
        testSeqTermString(nar, "(&/, a, /3, b, /5, c, /10, d)");
    }

    static void testSeqTermString(NAR nar, String s) {
        assertEquals(s, nar.term(s).toString());
    }

    @Test public void testTrailingSequenceInterval() {
        //trailing suffix interval not removed ordinarily
        assertEquals("$0.50;0.80;0.95$ <(&/, x, /1) =/> y>. :0: %1.00;0.90%",
              t.task("<(&/, x, /1) =/> y>.").toString());
    }
    @Test public void testTrailingSequenceIntervalRemovedIfTaskTerm() {
        //trailing suffix removed as the top-level term
        assertEquals("$0.50;0.80;0.95$ <a --> b>. :0: %1.00;0.90%",
              t.inputTask("(&/, <a --> b>, /1).").toString());
    }
    @Test public void testInvalidSequenceAsTask() {
        //the suffix will remove, x will fall through, and not be valid task term
        assertNull(t.inputTask("(&/, x, /1)."));
    }


//    @Test public void testSequenceSentenceNormalization() {
//        //sequences at the top level as terms must not have any trailing intervals
//        NAR nar = new Terminal();
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

        Sequence a = t.term("(&/, x, /1, y)");
        Sequence b = t.term("(&/, x, /2, y)");
        assertTrue(a!=b);
        assertEquals(1, a.distance1(b));
        assertEquals(1, b.distance1(a));
        assertEquals(0, a.distance1(a));

        Sequence c = t.term("(&/, x, /1, y)");
        Sequence d = t.term("(&/, x, /9, y)");
        assertEquals(Long.MAX_VALUE, c.distance1(d, 2));
        assertEquals(1, c.distance1(b, 2));


    }

    @Test public void testDoesntLoseInfo() {
        NAR nar = new Terminal();
        Term t = nar.term("<(&/, <$1 --> (/, open, _, door)>, /5) =/> <$1 --> (/, enter, _, room)>>");
        Term t2 = t.normalized();
        String s1 = t.toString();
        String s2 = t2.toString();
        assert s1.contains("/5");
        assertEquals(s1,s2);
    }

    @Test public void testDoesntDeriveTemporalsFromEternal1() {
        //the eternal inputs here should not derive anything temporal

        /*
        ((<%1 ==> %2>, %3, not_implication_or_equivalence(%3)), (<(&|, %1, %3) ==> %2>, (<Induction --> Truth>, <ForAllSame --> Order>)))
         */
        NAR nar = new Default(200,4,3,2);
        nar.nal(7);
        nar.log();
        nar.believe("<<{(#1, #2)} --> [commutatorArgument2]> ==> <{#1} --> [LinearOperator]>>.");
        nar.believe("<(commutesWith, \"commutes with\") --> label>.");

        //detect any &/ and &| derivations which are incorrect
        nar.memory.eventDerived.on(t -> {
            String ts = t.toString();
            if (    (ts.contains("&/")) ||
                    (ts.contains("&|")) )
                assertFalse(true);
        });

        nar.frame(96);

    }

    @Test public void testSequenceToArrayWithIntervals() {
        Sequence a = $("(&/, x, /3, y)");
        assertEquals("[x, y]", Arrays.toString(a.terms()));
        assertEquals("[x, /3, y]", Arrays.toString(a.toArrayWithIntervals()));
        assertEquals("[x, /3]", Arrays.toString(a.toArrayWithIntervals((i,x) -> i==0)));
        assertEquals("[y]", Arrays.toString(a.toArrayWithIntervals((i,x) -> i==1)));
    }
}