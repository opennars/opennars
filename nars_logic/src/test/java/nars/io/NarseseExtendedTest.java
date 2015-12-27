package nars.io;

import nars.Narsese;
import nars.Op;
import nars.Symbols;
import nars.nal.nal7.Tense;
import nars.nar.Terminal;
import nars.task.Task;
import nars.term.Term;
import nars.term.compound.Compound;
import org.junit.Test;

import static nars.io.NarseseTest.task;
import static nars.io.NarseseTest.term;
import static nars.nal.nal7.Tense.*;
import static org.junit.Assert.*;

/**
 * Proposed syntax extensions, not implemented yet
 */
public class NarseseExtendedTest {



    void eternal(Task t) {
        tensed(t, true, null);
    }
    void tensed(Task t, Tense w) {
        tensed(t, false, w);
    }
    void tensed(Task t, boolean eternal, Tense w) {
        assertEquals(eternal, isEternal(t.getOccurrenceTime()));
        if (!eternal) {
            switch (w) {
                case Past: assertTrue(t.getOccurrenceTime() < 0); break;
                case Future: assertTrue(t.getOccurrenceTime() > 0); break;
                case Present: assertEquals(0, t.getOccurrenceTime()); break;
            }
        }
    }

    @Test public void testOriginalTruth() {
        //singular form, normal, to test that it still works
        eternal(task("(a & b). %1.0%"));

        //normal, to test that it still works
        tensed(task("(a & b). :|: %1.0%"), Present);
    }

    /** compact representation combining truth and tense */
    @Test public void testTruthTense() {


        tensed(task("(a & b). %1.0|0.7%"), Present);

        tensed(task("(a & b). %1.0/0.7%"), Future);
        tensed(task("(a & b). %1.0\\0.7%"), Past);
        eternal(task("(a & b). %1.0;0.7%"));

        /*tensed(task("(a & b). %1.0|"), Present);
        tensed(task("(a & b). %1.0/"), Future);
        tensed(task("(a & b). %1.0\\"), Past);*/
        eternal(task("(a & b). %1.0%"));





    }

    @Test public void testQuestionTenseOneCharacter() {
        //TODO one character tense for questions/quests since they dont have truth values
    }

    @Test
    public void testColonReverseInheritance() {
        Compound t = term("namespace:named");
        assertEquals(t.op(), Op.INHERIT);
        assertEquals("named", t.term(0).toString());
        assertEquals("namespace", t.term(1).toString());



        Compound u = term("<a:b --> c:d>");
        assertEquals("<<b-->a>--><d-->c>>", u.toString());

        Task ut = task("<a:b --> c:d>.");
        assertNotNull(ut);
        assertEquals(ut.term(), u);

    }
//    @Test
//    public void testBacktickReverseInstance() {
//        Inheritance t = term("namespace`named");
//        assertEquals(t.op(), Op.INHERITANCE);
//        assertEquals("namespace", t.getPredicate().toString());
//        assertEquals("{named}", t.getSubject().toString());
//    }


    static void eqTerm(String shorter, String expected) {
        Narsese p = Narsese.the();

        Term a = p.term(shorter);
        assertNotNull(a);
        assertEquals(expected, a.toString());

        eqTask(shorter, expected);
    }

    static final Terminal t = new Terminal();

    static void eqTask(String x, String b) {
        Task a = t.task(x + '.');
        assertNotNull(a);
        assertEquals(b, a.term().toString());
    }

    @Test
    public void testNamespaceTerms2() {
        eqTerm("a:b", "<b-->a>");
        eqTerm("a : b", "<b-->a>");
    }

    @Test public void testNamespaceTermsNonAtomicSubject() {
        eqTerm("c:{a,b}", "<{a,b}-->c>");
    }
    @Test public void testNamespaceTermsNonAtomicPredicate() {
        eqTerm("<a-->b>:c", "<c--><a-->b>>");
        eqTerm("{a,b}:c", "<c-->{a,b}>");
        eqTerm("(a,b):c", "<c-->(a,b)>");
    }

    @Test public void testNamespaceTermsChain() {

        eqTerm("d:{a,b}:c", "<<c-->{a,b}>-->d>");


        eqTerm("c:{a,b}", "<{a,b}-->c>");
        eqTerm("a:b:c",   "<<c-->b>-->a>");
        eqTerm("a :b :c",   "<<c-->b>-->a>");
    }

    @Test
    public void testNamespaceLikeJSON() {
        Narsese p = Narsese.the();
        Term a = p.term("{ a:x, b:{x,y} }");
        assertNotNull(a);
        assertEquals(p.term("{<{x,y}-->b>, <x-->a>}"), a);

    }

    @Test
    public void testNegation2() throws Narsese.NarseseException {


        for (String s : new String[]{"--negated!", "-- negated!"}) {
            Task t = task(s);

            //System.out.println(t);
            /*
            (--,(negated))! %1.00;0.90% {?: 1}
            (--,(negated))! %1.00;0.90% {?: 2}
            */

            Term tt = t.term();
            assertEquals(Op.NEGATE, tt.op());
            assertTrue("negated".equals(((Compound) tt).term(0).toString()));
            assertTrue(t.getPunctuation() == Symbols.GOAL);
        }
    }

    @Test
    public void testNegation3() {
        //without comma
        assertEquals( "(--,x)", term("--x").toStringCompact() );
        assertEquals( "(--,x)", term("-- x").toStringCompact() );

        assertEquals( "(--,(&&,x,y))", term("-- (x && y)").toStringCompact() );


        Compound nab = term("--(a & b)");
        assertTrue(nab.op(Op.NEGATE));

        assertTrue(nab.term(0).op(Op.INTERSECT_EXT));

//        try {
//            task("(-- negated illegal_extra_term)!");
//            assertTrue(false);
//        } catch (Exception e) {
//        }
    }
}
