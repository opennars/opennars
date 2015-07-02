package nars.nal;

import nars.nal.nal1.Inheritance;
import nars.task.Task;
import nars.term.Compound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Proposed syntax extensions, not implemented yet
 */
public class NarseseParserExtendedTest  {



    @Test
    public void testNamespaceTerms() {
        Inheritance t = NarseseParserTest.term("namespace.named");
        assertEquals(t.operator(), NALOperator.INHERITANCE);
        assertEquals("namespace", t.getPredicate().toString());
        assertEquals("named", t.getSubject().toString());


        Compound u = NarseseParserTest.term("<a.b --> c.d>");
        assertEquals("<<b --> a> --> <d --> c>>", u.toString());

        Task ut = NarseseParserTest.task("<a.b --> c.d>.");
        assertNotNull(ut);
        assertEquals(ut.getTerm(), u);

    }


//    @Test
//    public void testNegation2() throws InvalidInputException {
//
//        for (String s : new String[]{"--negated!", "-- negated!"}) {
//            Task t = task(s);
//            Term tt = t.getTerm();
//            assertTrue(tt instanceof Negation);
//            assertTrue(((Negation) tt).the().toString().equals("negated"));
//            assertTrue(t.getPunctuation() == Symbols.GOAL);
//        }
//    }

//    @Test
//    public void testNegation3() {
//        Negation nab = term("--(a & b)");
//        assertTrue(nab instanceof Negation);
//        IntersectionExt ab = (IntersectionExt) nab.the();
//        assertTrue(ab instanceof IntersectionExt);
//
//        try {
//            task("(-- negated illegal_extra_term)!");
//            assertTrue(false);
//        } catch (Exception e) {
//        }
//    }
}
