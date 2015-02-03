package nars.logic;

import nars.build.Default;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.Symbols;
import nars.io.narsese.NarseseParser;
import nars.logic.entity.CompoundTerm;
import nars.logic.entity.Task;
import nars.logic.nal1.Inheritance;
import nars.logic.nal3.Intersect;
import nars.logic.nal3.IntersectionExt;
import nars.logic.nal3.IntersectionInt;
import nars.logic.nal4.Product;
import org.junit.Test;

import static org.junit.Assert.*;


public class NarseseParserTest {

    final static NAR n = new NAR(new Default());
    final static NarseseParser p = NarseseParser.newParser(n);

    @Test public void testParseCompleteEternalTask() {
        Task t = p.parseTask("$0.99;0.95$ <a --> b>! %0.93;0.95%");

        assertNotNull(t);
        assertEquals('!', t.getPunctuation());
        assertEquals(0.99f, t.budget.getPriority(), 0.001);
        assertEquals(0.95f, t.budget.getDurability(), 0.001);
        assertEquals(0.93f, t.sentence.getTruth().getFrequency(), 0.001);
        assertEquals(0.95f, t.sentence.getTruth().getConfidence(), 0.001);
    }

    @Test public void testIncompleteTask() {
        Task t = p.parseTask("<a --> b>.");
        assertNotNull(t);
        assertEquals(Symbols.NALOperator.INHERITANCE, t.sentence.term.operator());
        Inheritance i = (Inheritance)t.getTerm();
        assertEquals("a", i.getSubject().toString());
        assertEquals("b", i.getPredicate().toString());
        assertEquals('.', t.getPunctuation());
        assertEquals(Parameters.DEFAULT_JUDGMENT_PRIORITY, t.budget.getPriority(), 0.001);
        assertEquals(Parameters.DEFAULT_JUDGMENT_DURABILITY, t.budget.getDurability(), 0.001);
        assertEquals(1f, t.sentence.getTruth().getFrequency(), 0.001);
        assertEquals(Parameters.DEFAULT_JUDGMENT_CONFIDENCE, t.sentence.getTruth().getConfidence(), 0.001);
    }

    @Test public void testNoBudget() {
        Task t = p.parseTask("<a <=> b>. %0.00;0.93");
        assertNotNull(t);
        assertEquals(Symbols.NALOperator.EQUIVALENCE, t.sentence.term.operator());

        assertEquals('.', t.getPunctuation());
        assertEquals(Parameters.DEFAULT_JUDGMENT_PRIORITY, t.budget.getPriority(), 0.001);
        assertEquals(Parameters.DEFAULT_JUDGMENT_DURABILITY, t.budget.getDurability(), 0.001);
        assertEquals(0f, t.sentence.getTruth().getFrequency(), 0.001);
        assertEquals(0.93f, t.sentence.getTruth().getConfidence(), 0.001);
    }

    @Test public void testMultiCompound() {
        String tt = "<<a <=> b> --> <c ==> d>>";
        Task t = p.parseTask(tt + "?");
        assertNotNull(t);
        assertEquals(Symbols.NALOperator.INHERITANCE, t.sentence.term.operator());
        assertEquals(tt, t.getTerm().toString());
        assertEquals('?', t.getPunctuation());
        assertNull(t.sentence.truth);
        assertEquals(7, t.getTerm().getComplexity());
    }

    protected void testProductABC(Product p) {
        assertEquals(3, p.size());
        assertEquals("a", p.term[0].toString());
        assertEquals("b", p.term[1].toString());
        assertEquals("c", p.term[2].toString());
    }

    @Test public void testProduct() {
        String tt = "(*,a,b,c)";
        Task t = p.parseTask(tt + "@");
        assertNotNull(t);
        assertEquals(Symbols.NALOperator.PRODUCT, t.sentence.term.operator());
        assertEquals(tt, t.getTerm().toString());
        assertEquals('@', t.getPunctuation());
        assertNull(t.sentence.truth);

        Product pt = (Product)t.getTerm();
        testProductABC(pt);
    }

    @Test public void testProductWithoutAsterisk() {

        Product pt = p.parseTerm("(a, b, c)");

        assertNotNull(pt);
        assertEquals(Symbols.NALOperator.PRODUCT, pt.operator());

        testProductABC(pt);

        testProductABC(p.parseTerm("(a,b,c)")); //without spaces
        testProductABC(p.parseTerm("(a, b, c)")); //additional spaces
        testProductABC(p.parseTerm("(a , b, c)")); //additional spaces
        testProductABC(p.parseTerm("(a , b , c)")); //additional spaces
        testProductABC(p.parseTerm("(a ,\tb, c)")); //tab
    }

    @Test public void testInfix2() {
        Intersect t = p.parseTerm("(x & y)");
        assertEquals(Symbols.NALOperator.INTERSECTION_EXT, t.operator());
        assertEquals(2, t.size());
        assertEquals("x", t.term[0].toString());
        assertEquals("y", t.term[1].toString());

        IntersectionInt a = p.parseTerm("(x | y)");
        assertEquals(Symbols.NALOperator.INTERSECTION_INT, a.operator());
        assertEquals(2, a.size());

        Product b = p.parseTerm("(x * y)");
        assertEquals(Symbols.NALOperator.PRODUCT, b.operator());
        assertEquals(2, b.size());

        CompoundTerm c = p.parseTerm("(<a -->b> && y)");
        assertEquals(Symbols.NALOperator.CONJUNCTION, c.operator());
        assertEquals(2, c.size());
        assertEquals(3, c.getComplexity());
        assertEquals(Symbols.NALOperator.INHERITANCE, c.term[0].operator());
    }
}
