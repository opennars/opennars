package nars.logic;

import nars.build.Default;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.Symbols;
import nars.io.narsese.InvalidInputException;
import nars.io.narsese.NarseseParser;
import nars.logic.entity.Compound;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.entity.Variable;
import nars.logic.nal1.Inheritance;
import nars.logic.nal3.Intersect;
import nars.logic.nal3.IntersectionInt;
import nars.logic.nal4.Product;
import nars.logic.nal7.Interval;
import nars.logic.nal8.Operation;
import org.junit.Test;

import static org.junit.Assert.*;


public class NarseseParserTest {

    final static NAR n = new NAR(new Default());
    final static NarseseParser p = NarseseParser.newParser(n);

    <T extends Term> T term(String s) throws InvalidInputException {
        //TODO n.term(s) when the parser is replaced
        return p.parseTerm(s);
    }

    Task task(String s) throws InvalidInputException {
        //TODO n.task(s) when the parser is replaced
        return p.parseTask(s);
    }


    @Test public void testSomethingTheOldParserCouldntHandle() {

        Task t = task("<<$A --> $B> --> QPre>!");
        assertNotNull(t);

        Task t1 = task("<<<$A --> $B> --> QPre> =|> X>!");
        assertNotNull(t);

        Task t2 = task("<<<$A --> $B> --> QPre> =|> <X-->Y>>!");
        assertNotNull(t);

        Task t3 = task("<<<$A --> $B> --> QPre> =|> <$A --> $B>>!");
        assertNotNull(t);

        System.out.println(t);
    }

    @Test public void testParseCompleteEternalTask() throws InvalidInputException {
        Task t = task("$0.99;0.95$ <a --> b>! %0.93;0.95%");

        assertNotNull(t);
        assertEquals('!', t.getPunctuation());
        assertEquals(0.99f, t.budget.getPriority(), 0.001);
        assertEquals(0.95f, t.budget.getDurability(), 0.001);
        assertEquals(0.93f, t.sentence.getTruth().getFrequency(), 0.001);
        assertEquals(0.95f, t.sentence.getTruth().getConfidence(), 0.001);
    }

    @Test public void testIncompleteTask() throws InvalidInputException {
        Task t = task("<a --> b>.");
        assertNotNull(t);
        assertEquals(NALOperator.INHERITANCE, t.sentence.term.operator());
        Inheritance i = (Inheritance)t.getTerm();
        assertEquals("a", i.getSubject().toString());
        assertEquals("b", i.getPredicate().toString());
        assertEquals('.', t.getPunctuation());
        assertEquals(Parameters.DEFAULT_JUDGMENT_PRIORITY, t.budget.getPriority(), 0.001);
        assertEquals(Parameters.DEFAULT_JUDGMENT_DURABILITY, t.budget.getDurability(), 0.001);
        assertEquals(1f, t.sentence.getTruth().getFrequency(), 0.001);
        assertEquals(Parameters.DEFAULT_JUDGMENT_CONFIDENCE, t.sentence.getTruth().getConfidence(), 0.001);
    }

    @Test public void testNoBudget() throws InvalidInputException {
        Task t = task("<a <=> b>. %0.00;0.93");
        assertNotNull(t);
        assertEquals(NALOperator.EQUIVALENCE, t.sentence.term.operator());

        assertEquals('.', t.getPunctuation());
        assertEquals(Parameters.DEFAULT_JUDGMENT_PRIORITY, t.budget.getPriority(), 0.001);
        assertEquals(Parameters.DEFAULT_JUDGMENT_DURABILITY, t.budget.getDurability(), 0.001);
        assertEquals(0f, t.sentence.getTruth().getFrequency(), 0.001);
        assertEquals(0.93f, t.sentence.getTruth().getConfidence(), 0.001);
    }

    @Test public void testMultiCompound() throws InvalidInputException {
        String tt = "<<a <=> b> --> <c ==> d>>";
        Task t = task(tt + "?");
        assertNotNull(t);
        assertEquals(NALOperator.INHERITANCE, t.sentence.term.operator());
        assertEquals(tt, t.getTerm().toString());
        assertEquals('?', t.getPunctuation());
        assertNull(t.sentence.truth);
        assertEquals(7, t.getTerm().getComplexity());
    }

    protected void testProductABC(Product p) throws InvalidInputException {
        assertEquals(p.toString() + " should have 3 sub-terms", 3, p.size());
        assertEquals("a", p.term[0].toString());
        assertEquals("b", p.term[1].toString());
        assertEquals("c", p.term[2].toString());
    }

    @Test public void testFailureOfMultipleDistinctInfixOperators() {

        try {
            term("(a * b & c)");
            assertTrue("exception should have been thrown", false);
        } catch (InvalidInputException e) {
            String s = e.toString();
            assertTrue(s.contains("&"));
            assertTrue(s.contains("contradicts"));
            assertTrue(s.contains("*"));
        }
    }

    @Test public void testQuest() throws InvalidInputException {
        String tt = "(*,a,b,c)";
        Task t = task(tt + "@");
        assertNotNull(t);
        assertEquals(NALOperator.PRODUCT, t.sentence.term.operator());
        assertEquals(tt, t.getTerm().toString());
        assertEquals('@', t.getPunctuation());
        assertNull(t.sentence.truth);

    }

    @Test public void testProduct() throws InvalidInputException {

        Product pt = term("(a, b, c)");

        assertNotNull(pt);
        assertEquals(NALOperator.PRODUCT, pt.operator());

        testProductABC(pt);

        testProductABC(term("(*,a,b,c)")); //with optional prefix
        testProductABC(term("(a,b,c)")); //without spaces
        testProductABC(term("(a, b, c)")); //additional spaces
        testProductABC(term("(a , b, c)")); //additional spaces
        testProductABC(term("(a , b , c)")); //additional spaces
        testProductABC(term("(a ,\tb, c)")); //tab
        testProductABC(term("(a b c)")); //without commas
        testProductABC(term("(a *  b * c)")); //with multiple (redundant) infix
    }

    @Test public void testInfix2() throws InvalidInputException {
        Intersect t = term("(x & y)");
        assertEquals(NALOperator.INTERSECTION_EXT, t.operator());
        assertEquals(2, t.size());
        assertEquals("x", t.term[0].toString());
        assertEquals("y", t.term[1].toString());

        IntersectionInt a = term("(x | y)");
        assertEquals(NALOperator.INTERSECTION_INT, a.operator());
        assertEquals(2, a.size());

        Product b = term("(x * y)");
        assertEquals(NALOperator.PRODUCT, b.operator());
        assertEquals(2, b.size());

        Compound c = term("(<a -->b> && y)");
        assertEquals(NALOperator.CONJUNCTION, c.operator());
        assertEquals(2, c.size());
        assertEquals(5, c.getComplexity());
        assertEquals(NALOperator.INHERITANCE, c.term[0].operator());
    }



    @Test public void testNegation() throws InvalidInputException {

    }

    protected void testBelieveAB(Operation t) {
        assertEquals(3, t.getArguments().size());
        assertEquals("^believe", t.getOperator().toString());
        assertEquals("a", t.getArgument(0).toString());
        assertEquals("b", t.getArgument(1).toString());
        assertEquals("SELF", t.getArgument(2).toString());
    }

    @Test public void testOperation() throws InvalidInputException {
        testBelieveAB(term("believe(a,b)"));
        testBelieveAB(term("believe(a,b,SELF)"));
        testBelieveAB(term("believe(a b)"));


        testBelieveAB(term("(^believe,a,b)"));
        testBelieveAB(term("(^believe,a,b,SELF)"));
        testBelieveAB(term("(^ believe,a,b)"));
        testBelieveAB(term("(^,believe,a,b)"));
        testBelieveAB(term("(^ believe a b)"));

    }

    @Test public void testInterval() throws InvalidInputException {

        Term x = term(Symbols.INTERVAL_PREFIX + "2");
        assertNotNull(x);
        assertEquals(Interval.class, x.getClass());
        Interval i = (Interval)x;
        assertEquals(1, i.magnitude);

    }

    protected Variable testVar(char prefix) {
        Term x = term(prefix + "x");
        assertNotNull(x);
        assertEquals(Variable.class, x.getClass());
        Variable i = (Variable)x;
        assertEquals(prefix + "x", i.name());
        return i;
    }

    @Test public void testVariables() throws InvalidInputException {
        Variable v;
        v = testVar(Symbols.VAR_DEPENDENT);
        assertTrue(v.hasVarDep());

        v = testVar(Symbols.VAR_INDEPENDENT);
        assertTrue(v.hasVarIndep());

        v = testVar(Symbols.VAR_QUERY);
        assertTrue(v.hasVarQuery());
    }

    @Test public void testTenses() throws InvalidInputException {

    }

    @Test public void testEscape() throws InvalidInputException {
        //TODO apply existing escaping tests?
    }

    @Test public void testFuzzyKeywords() throws InvalidInputException {
        //definately=certainly, uncertain, doubtful, dubious, maybe, likely, unlikely, never, always, yes, no, sometimes, usually, rarely, etc...
        //ex: %maybe never%, % doubtful always %, %certainly never%
    }

    @Test public void testEmbeddedJavascript() throws InvalidInputException {

    }

    @Test public void testEmbeddedPrologRules() throws InvalidInputException {

    }

    /** test ability to report meaningful parsing errors */
    @Test public void testError() {

    }

}
