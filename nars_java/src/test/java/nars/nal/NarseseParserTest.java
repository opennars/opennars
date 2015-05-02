package nars.nal;

import nars.budget.Budget;
import nars.prototype.Default;
import nars.NAR;
import nars.Global;
import nars.io.Symbols;
import nars.io.narsese.InvalidInputException;
import nars.io.narsese.NarseseParser;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.Variable;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal3.Intersect;
import nars.nal.nal3.IntersectionExt;
import nars.nal.nal3.IntersectionInt;
import nars.nal.nal4.Product;
import nars.nal.nal7.Interval;
import nars.nal.nal8.Operation;
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
        return p.parseTask(s, true);
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
        assertEquals(0.99f, t.getPriority(), 0.001);
        assertEquals(0.95f, t.getDurability(), 0.001);
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
        assertEquals(Global.DEFAULT_JUDGMENT_PRIORITY, t.getPriority(), 0.001);
        assertEquals(Global.DEFAULT_JUDGMENT_DURABILITY, t.getDurability(), 0.001);
        assertEquals(1f, t.sentence.getTruth().getFrequency(), 0.001);
        assertEquals(Global.DEFAULT_JUDGMENT_CONFIDENCE, t.sentence.getTruth().getConfidence(), 0.001);
    }

    @Test public void testPropertyInstance() {

        taskEqualsOldParser("<a --] b>.");
        taskEqualsOldParser("<a {-- b>.");
        taskEqualsOldParser("<a {-] b>.");
    }

    @Test public void testNoBudget() throws InvalidInputException {
        Task t = task("<a <=> b>. %0.00;0.93");
        assertNotNull(t);
        assertEquals(NALOperator.EQUIVALENCE, t.sentence.term.operator());

        assertEquals('.', t.getPunctuation());
        assertEquals(Global.DEFAULT_JUDGMENT_PRIORITY, t.getPriority(), 0.001);
        assertEquals(Global.DEFAULT_JUDGMENT_DURABILITY, t.getDurability(), 0.001);
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
        //testProductABC(term("(a b c)")); //without commas
        //testProductABC(term("(a *  b * c)")); //with multiple (redundant) infix
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
        assertEquals(NALOperator.INHERITANCE, c.term[1].operator());
    }


    @Test public void testShortFloat() {
        taskEqualsOldParser("<{Willy} --> [black]>. %0%");
    }

    @Test public void testNegation() throws InvalidInputException {
        taskEqualsOldParser("(--,negated).");
        taskEqualsOldParser("(--, negated).");
    }

    @Test public void testNegation2() throws InvalidInputException {

        for (String s : new String[] { "--negated!", "-- negated!" }) {
            Task t = task(s);
            Term tt = t.getTerm();
            assertTrue(tt instanceof Negation);
            assertTrue(((Negation) tt).negated().toString().equals("negated"));
            assertTrue(t.getPunctuation() == Symbols.GOAL);
        }
    }

    @Test public void testNegation3() {
        Negation nab = term("--(a & b)");
        assertTrue(nab instanceof Negation);
        IntersectionExt ab = (IntersectionExt) nab.negated();
        assertTrue(ab instanceof IntersectionExt);

        try {
            task("(-- negated illegal_extra_term)!");
            assertTrue(false);
        }
        catch (Exception e) { }
    }


    protected void testBelieveAB(Operation t) {
        assertEquals(3, t.getArguments().size());
        assertEquals("^believe", t.getOperator().toString());
        assertEquals("a", t.getArgument(0).toString());
        assertEquals("b", t.getArgument(1).toString());
        assertEquals("SELF", t.getArgument(2).toString());
    }

    @Test public void testOperationNoArgs() {
        taskEqualsOldParser("believe()!");
        taskEqualsOldParser("believe( )!");
    }

    @Test public void testOperation() throws InvalidInputException {

        testBelieveAB(term("(^believe,a,b)"));
        testBelieveAB(term("(^believe,a,b,SELF)"));
        //testBelieveAB(term("(^ believe,a,b)"));
        //testBelieveAB(term("(^,believe,a,b)"));
        //testBelieveAB(term("(^ believe a b)"));

    }

    @Test public void testOperation2() throws InvalidInputException {
        testBelieveAB(term("believe(a,b)"));
        testBelieveAB(term("believe(a,b,SELF)"));
        //testBelieveAB(term("believe(a b)"));

    }

    @Test public void testOperationTask() {
        taskEqualsOldParser("(^break,{t001},SELF)! %1.00;0.95%");
    }

    @Test public void testInterval() throws InvalidInputException {

        Term x = term(Symbols.INTERVAL_PREFIX + "2");
        assertNotNull(x);
        assertEquals(Interval.class, x.getClass());
        Interval i = (Interval)x;
        assertEquals(1, i.magnitude);

    }

    @Test public void testCompoundTermOpenerCloserStatements() {
        Term a = term("<a --> b>");
        Term x = term("(a --> b)");
        Term y = term("(a-->b)");
        assertEquals(NALOperator.INHERITANCE, x.operator());
        assertEquals(x, a);
        assertEquals(x, y);

        assertNotNull(term("((a,b)-->c)")); //intermediate
        assertNotNull(term("((a,b) --> c)")); //intermediate
        assertNotNull(term("<(a,b) --> c>")); //intermediate
        assertNotNull(term("<a --> (c,d)>")); //intermediate
        assertNotNull(term("<a-->(c,d)>")); //intermediate
        assertNotNull(term("(a-->(c,d))")); //intermediate
        assertNotNull(term("(a --> (c,d))")); //intermediate

        Term abcd = term("((a,b) --> (c,d))");
        Term ABCD = term("<(*,a,b) --> (*,c,d)>");
        assertEquals(NALOperator.INHERITANCE, x.operator());
        assertEquals(abcd + " != " + ABCD, abcd, ABCD);
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

    @Test public void testSet() {
        Compound xInt = term("[x]");
        assertEquals(NALOperator.SET_INT_OPENER, xInt.operator());
        assertEquals(1, xInt.size());
        assertEquals("x", xInt.term[0].toString());

        Compound xExt = term("{x}");
        assertEquals(NALOperator.SET_EXT_OPENER, xExt.operator());
        assertEquals(1, xExt.size());
        assertEquals("x", xExt.term[0].toString());

        Compound abInt = term("[a,b]");
        assertEquals(2, abInt.size());
        assertEquals("a", abInt.term[0].toString());
        assertEquals("b", abInt.term[1].toString());

        assertEquals(abInt, term("[ a,b]"));
        assertEquals(abInt, term("[a,b ]"));
        assertEquals(abInt, term("[ a , b ]"));


    }

    @Test public void testTenses() throws InvalidInputException {
        taskEqualsOldParser("<a --> b>. :|:");
        taskEqualsOldParser("<a --> b>. :/:");
        taskEqualsOldParser("<a --> b>. :\\:");
    }

    @Test public void testEscape() throws InvalidInputException {
        taskEqualsOldParser("<a --> \"a\">.");
        assertTrue( task("<a --> \"a\">.").toString().contains("<a --> \"a\">.") );
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

    @Test public void testSimpleTask() {
        taskEqualsOldParser("(-,mammal,swimmer). %0.00;0.90%");

    }
    @Test public void testCompleteTask() {
        taskEqualsOldParser("$0.80;0.50;0.95$ <<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>. %1.00;0.90%");
    }

    @Test public void testImageIndex() {
        Compound t = term("(/,open,$1,_)");
        assertEquals("(/,open,$1,_)", t.toString());
        assertEquals("index psuedo-term should not count toward its size", 2, t.size());
    }

    private void taskEqualsOldParser(String s) {
        Task t = task(s);
        assertNotNull(t);
        Task u = n.narsese.parseTask(s);
        assertNotNull(u);

        assertEquals("(term) " + t + " != " + u, u.getTerm(), t.getTerm());
        assertEquals("(truth) " + t.sentence.truth + " != " + u.sentence.truth, u.sentence.truth, t.sentence.truth);
        assertEquals("(creationTime) " + u.getCreationTime() + " != " + t.getCreationTime(), u.getCreationTime(), t.getCreationTime());
        assertEquals("(occurencetime) " + u.getOccurrenceTime() + " != " + t.getOccurrenceTime(), u.getOccurrenceTime(), t.getOccurrenceTime());
        //TODO budget:
        //TODO punctuation:
    }

    @Test public void testNamespaceTerms() {
        Inheritance t = term("namespace.named");
        assertEquals(t.operator(), NALOperator.INHERITANCE);
        assertEquals("namespace", t.getPredicate().toString());
        assertEquals("named", t.getSubject().toString());


        Compound u = term("<a.b <-> c.d>");
        assertEquals("<<b --> a> <-> <d --> c>>", u.toString());

        Task ut = task("<a.b <-> c.d>.");
        assertNotNull(ut);
        assertEquals(ut.getTerm(), u);

    }
}
