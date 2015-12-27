package nars.io;

import nars.*;
import nars.nal.nal8.Operator;
import nars.nar.Terminal;
import nars.op.io.echo;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.term.variable.Variable;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class NarseseTest {

    static final NAR n = new Terminal();
    static final Narsese p = Narsese.the();

    static <T extends Term> T term(String s) throws Narsese.NarseseException {
        //TODO n.term(s) when the parser is replaced
        return p.termRaw(s);
    }

    static List<Task> tasks(String s) throws Narsese.NarseseException {
        //TODO n.task(s) when the parser is replaced
        //return p.parseTask(s, true);
        List<Task> l = Global.newArrayList(1);
        p.tasks(s, l, n.memory);
        return l;
    }


    static Task task(String s) throws Narsese.NarseseException {
        List<Task> l = tasks(s);
        if (l.size() != 1)
            throw new RuntimeException("Expected 1 task, got: " + l);
        return l.get(0);
    }


//    @Test @Ignore
//    public void testSomethingTheOldParserCouldntHandle() {
//
//        Task t = task("<<$A --> $B> --> QPre>!");
//        assertNotNull(t);
//
//        Task t1 = task("<<<$A --> $B> --> QPre> =|> X>!");
//        assertNotNull(t);
//
//        Task t2 = task("<<<$A --> $B> --> QPre> =|> <X-->Y>>!");
//        assertNotNull(t);
//
//        Task t3 = task("<<<$A --> $B> --> QPre> =|> <$A --> $B>>!");
//        assertNotNull(t);
//
//        System.out.println(t);
//    }

    @Test
    public void testParseCompleteEternalTask() throws Narsese.NarseseException {
        Task t = task("$0.99;0.95$ <a --> b>! %0.93;0.95%");

        assertNotNull(t);
        assertEquals('!', t.getPunctuation());
        assertEquals(0.99f, t.getPriority(), 0.001);
        assertEquals(0.95f, t.getDurability(), 0.001);
        assertEquals(0.93f, t.getFrequency(), 0.001);
        assertEquals(0.95f, t.getConfidence(), 0.001);
    }

    @Test
    public void testIncompleteTask() throws Narsese.NarseseException {
        Task t = task("<a --> b>.");
        assertNotNull(t);
        assertEquals(Op.INHERIT, t.op());
        Compound i = t.term();
        assertEquals("a", i.term(0).toString());
        assertEquals("b", i.term(1).toString());
        assertEquals('.', t.getPunctuation());
        //assertEquals(Global.DEFAULT_JUDGMENT_PRIORITY, t.getPriority(), 0.001);
        //assertEquals(Global.DEFAULT_JUDGMENT_DURABILITY, t.getDurability(), 0.001);
        assertEquals(1.0f, t.getTruth().getFrequency(), 0.001);
        //assertEquals(Global.DEFAULT_JUDGMENT_CONFIDENCE, t.getTruth().getConfidence(), 0.001);
    }

    @Test
    public void testPropertyInstance() {

        taskParses("<a --] b>.");
        taskParses("<a {-- b>.");
        taskParses("<a {-] b>.");
    }

    @Test
    public void testBudget() throws Narsese.NarseseException {
        Task t = task("$0.70;0.60;0.50$ <a <=> b>. %0.00;0.93");
        assertEquals(0.7f, t.getPriority(), 0.01f);
        assertEquals(0.6f, t.getDurability(), 0.01f);
        assertEquals(0.5f, t.getQuality(), 0.01f);

        Task u = task("$0.9$ <a <=> b>. %0.00;0.93");
        assertEquals(0.9f, u.getPriority(), 0.01f);
    }

    @Test
    public void testNoBudget() throws Narsese.NarseseException {
        Task t = task("<a <=> b>. %0.00;0.93");
        assertNotNull(t);
        assertEquals(Op.EQUIV, t.op());

        assertEquals('.', t.getPunctuation());
        //assertEquals(Global.DEFAULT_JUDGMENT_PRIORITY, t.getPriority(), 0.001);
        //assertEquals(Global.DEFAULT_JUDGMENT_DURABILITY, t.getDurability(), 0.001);
        assertEquals(0.0f, t.getFrequency(), 0.001);
        assertEquals(0.93f, t.getConfidence(), 0.001);
    }

    @Test
    public void testMultiCompound() throws Narsese.NarseseException {
        String tt = "<<a<=>b>--><c==>d>>";
        Task t = task(tt + '?');
        assertNotNull(t);
        assertEquals(Op.INHERIT, t.op());
        assertEquals(tt, t.term().toString());
        assertEquals('?', t.getPunctuation());
        assertNull(t.getTruth());
        assertEquals(7, t.term().complexity());
    }

    protected void testProductABC(Compound p) throws Narsese.NarseseException {
        assertEquals(p.toString() + " should have 3 sub-terms", 3, p.size());
        assertEquals("a", p.term(0).toString());
        assertEquals("b", p.term(1).toString());
        assertEquals("c", p.term(2).toString());
    }

    @Test
    public void testFailureOfMultipleDistinctInfixOperators() {

        assertEquals(null, term("(a * b & c)"));

//        try {
//
//            assertTrue(invalid.toString() + " exception should have been thrown", false);
//        } catch (Narsese.NarseseException e) {
//            String s = e.toString();
//            assertTrue(s.contains("&"));
//            assertTrue(s.contains("*"));
//        }
    }

    @Test
    public void testQuest() throws Narsese.NarseseException {
        String tt = "(a,b,c)";
        Task t = task(tt + '@');
        assertNotNull(t);
        assertEquals(Op.PRODUCT, t.op());
        assertEquals(tt, t.term().toString());
        assertEquals('@', t.getPunctuation());
        assertNull(t.getTruth());

    }

    @Test
    public void testProduct() throws Narsese.NarseseException {

        Compound pt = term("(a, b, c)");

        assertNotNull(pt);
        assertEquals(Op.PRODUCT, pt.op());

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

    @Test
    public void testInfix2() throws Narsese.NarseseException {
        Compound t = term("(x & y)");
        assertEquals(Op.INTERSECT_EXT, t.op());
        assertEquals(2, t.size());
        assertEquals("x", t.term(0).toString());
        assertEquals("y", t.term(1).toString());

        Compound a = term("(x | y)");
        assertEquals(Op.INTERSECT_INT, a.op());
        assertEquals(2, a.size());

        Compound b = term("(x * y)");
        assertEquals(Op.PRODUCT, b.op());
        assertEquals(2, b.size());

        Compound c = term("(<a -->b> && y)");
        assertEquals(Op.CONJUNCTION, c.op());
        assertEquals(2, c.size());
        assertEquals(5, c.complexity());
        assertEquals(Op.INHERIT, c.term(1).op());
    }


    @Test
    public void testShortFloat() {

        taskParses("<{a} --> [b]>. %0%");
        taskParses("<a --> b>. %0.95%");
        taskParses("<a --> b>. %0.9%");
        taskParses("<a --> b>. %1%");
        taskParses("<a --> b>. %1.0%");
    }

    @Test
    public void testNegation() throws Narsese.NarseseException {
        taskParses("(--,negated).");
        taskParses("(--, negated).");

        assertEquals("(--,negated)", term("(--, negated)").toString());

    }



    protected void testBelieveAB(Compound t) {
        Term[] aa = Operator.opArgsArray(t);
        assertEquals(2, aa.length);
        assertEquals("^believe", t.term(1).toString());
        assertEquals("believe", Operator.operatorName(t).toString());
        assertEquals("a", aa[0].toString());
        assertEquals("b", aa[1].toString());
    }

    @Test
    public void testOperationNoArgs() {
        taskParses("believe()!");
        taskParses("believe( )!");
    }



    @Test
    public void testOperation2() throws Narsese.NarseseException {
        testBelieveAB(term("believe(a,b)"));
        testBelieveAB(term("believe(a, b)"));
    }

    @Test
    public void testOperationEquivalence() throws Narsese.NarseseException {
        Term a, b;
        a = term("a(b,c)");
        b = term("<(b,c) --> ^a>");
        assertEquals(a.op(), b.op());
        assertEquals(a.getClass(), b.getClass());
        assertEquals(a, b);



    }

    @Test
    public void testOperationTask() {
        taskParses("break({t001},SELF)! %1.00;0.95%");
    }



    @Test
    public void testCompoundTermOpenerCloserStatements() {
        Term a = term("<a --> b>");
        Term x = term("(a --> b)");
        Term y = term("(a-->b)");
        assertEquals(Op.INHERIT, x.op());
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
        assertEquals(Op.INHERIT, x.op());
        assertEquals(abcd + " != " + ABCD, abcd, ABCD);
    }

    protected Variable testVar(char prefix) {
        Term x = term(prefix + "x");
        assertNotNull(x);
        assertTrue(x instanceof Variable);
        Variable i = (Variable) x;
        assertEquals(prefix + "x", i.toString());
        return i;
    }

    @Test
    public void testVariables() throws Narsese.NarseseException {
        Variable v;
        v = testVar(Symbols.VAR_DEPENDENT);
        assertTrue(v.hasVarDep());

        v = testVar(Symbols.VAR_INDEPENDENT);
        assertTrue(v.hasVarIndep());

        v = testVar(Symbols.VAR_QUERY);
        assertTrue(v.hasVarQuery());
    }

    @Test
    public void testSet() {
        Compound xInt = term("[x]");
        assertEquals(Op.SET_INT_OPENER, xInt.op());
        assertEquals(1, xInt.size());
        assertEquals("x", xInt.term(0).toString());

        Compound xExt = term("{x}");
        assertEquals(Op.SET_EXT_OPENER, xExt.op());
        assertEquals(1, xExt.size());
        assertEquals("x", xExt.term(0).toString());

        Compound abInt = term("[a,b]");
        assertEquals(2, abInt.size());
        assertEquals("a", abInt.term(0).toString());
        assertEquals("b", abInt.term(1).toString());

        assertEquals(abInt, term("[ a,b]"));
        assertEquals(abInt, term("[a,b ]"));
        assertEquals(abInt, term("[ a , b ]"));


    }

    @Test
    public void testTenses() throws Narsese.NarseseException {
        Task now = task("<a --> b>. :|:");
        Task f = task("<a --> b>. :/:");
        Task p = task("<a --> b>. :\\:");
        assertTrue(now.getOccurrenceTime() > p.getOccurrenceTime());
        assertTrue(now.getOccurrenceTime() < f.getOccurrenceTime());
    }

    @Test
    public void testEscape() throws Narsese.NarseseException {
        taskParses("<a --> \"a\">.");
        assertTrue(task("<a --> \"a\">.").toString().contains("<a-->\"a\">."));
    }

    @Test
    public void testFuzzyKeywords() throws Narsese.NarseseException {
        //definately=certainly, uncertain, doubtful, dubious, maybe, likely, unlikely, never, always, yes, no, sometimes, usually, rarely, etc...
        //ex: %maybe never%, % doubtful always %, %certainly never%
    }

    @Test
    public void testEmbeddedJavascript() throws Narsese.NarseseException {

    }

    @Test
    public void testEmbeddedPrologRules() throws Narsese.NarseseException {

    }

    /**
     * test ability to report meaningful parsing errors
     */
    @Test
    public void testError() {

    }

    @Test
    public void testSimpleTask() {
        taskParses("(-,mammal,swimmer). %0.00;0.90%");

    }

    @Test
    public void testCompleteTask() {
        taskParses("$0.80;0.50;0.95$ <<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>. %1.00;0.90%");
    }

    @Test public void testNonNegativeIntegerAtoms() {
        //TODO test parsing to numeric atom types
        Atom a = term("1");
        assertEquals("1", a.toString());
    }

    @Test public void testNegativeIntegerAtoms() {
        //TODO test parsing to numeric atom types
        Atom a = term("-1");
        assertNotNull(a);
        assertEquals("-1", a.toString());
    }
    @Test public void testFloatAtom() {
        //TODO test parsing to numeric atom types
        float f = 1.24f;
        String ff = Float.toString(f);
        Atom a = term(ff);
        assertNotNull(a);
        assertEquals(ff, a.toString());
    }

    @Test
    public void testImageIndex() {
        Compound t = term("(/,open,$1,_)");
        assertEquals("(/,open,$1,_)", t.toString(false));
        assertEquals("(/, open, $1, _)", t.toString(true));
        assertEquals("index psuedo-term should not count toward its size", 2, t.size());
    }

    @Test
    public void testImageExtRel0() { testImageExtRel("<a-->(/,_,y,z)>", 0); }
    @Test
    public void testImageIntRel0() { testImageIntRel("<(\\,_,y,z)-->a>", 0); }

    @Test
    public void testImageExtRel1() { testImageExtRel("<a-->(/,x,_,z)>", 1); }
    @Test
    public void testImageIntRel1() { testImageIntRel("<(\\,x,_,z)-->a>", 1); }

    @Test
    public void testImageExtRel2() { testImageExtRel("<a-->(/,x,y,_)>", 2); }
    @Test
    public void testImageIntRel2() { testImageIntRel("<(\\,x,y,_)-->a>", 2); }

    private void testImageIntRel(String imageTerm, int relationIndexExpected) {
        Compound ti = term(imageTerm);
        assertEquals(relationIndexExpected, ((Compound)ti.term(0)).relation()  );
        assertEquals(imageTerm, ti.toString());
    }

    private void testImageExtRel(String imageTerm, int relationIndexExpected) {
        Compound ti = term(imageTerm);
        assertEquals(relationIndexExpected, ((Compound)ti.term(1)).relation() );
        assertEquals(imageTerm, ti.toString());
    }

    private void taskParses(String s) {
        Task t = task(s);
        assertNotNull(t);
//        Task u = oldParser.parseTaskOld(s, true);
//        assertNotNull(u);
//
//        assertEquals(u.getTerm() + " != " + t.getTerm(), u.getTerm(), t.getTerm());
//        assertEquals("(truth) " + t.getTruth() + " != " + u.getTruth(), u.getTruth(), t.getTruth());
//        //assertEquals("(creationTime) " + u.getCreationTime() + " != " + t.getCreationTime(), u.getCreationTime(), t.getCreationTime());
//        assertEquals("(occurrencetime) " + u.getOccurrenceTime() + " != " + t.getOccurrenceTime(), u.getOccurrenceTime(), t.getOccurrenceTime());
        //TODO budget:
        //TODO punctuation:
    }


    @Test
    public void testMultiline() {
        String a = "<a --> b>.";
        assertEquals(1, tasks(a).size());

        String b = "<a --> b>. <b --> c>.";
        assertEquals(2, tasks(b).size());

        String c = "<a --> b>. \n <b --> c>.";
        assertEquals(2, tasks(c).size());

        String s = "<a --> b>.\n" +
                "<b --> c>.\n" +

                "<multi\n" +
                " --> \n" +
                "line>. :|:\n" +

                "<multi \n" +
                " --> \n" +
                "line>.\n" +

                "<x --> b>!\n" +
                "<y --> w>.  <z --> x>.\n";

        List<Task> t = tasks(s);
        assertEquals(7, t.size());

    }

    @Test
    public void testMultilineQuotes() {
        String a = "js(\"\"\"\n" + "1\n" + "\"\"\")!";
        List<Task> l = tasks(a);
        assertEquals(1, l.size());
    }

    @Test
    public void testLineComment() {
        String a = "<a --> b>.\n//comment1234\n<b-->c>.";
        List<Task> l = tasks(a);
        assertEquals(3, l.size());
        Compound op = ((Task)l.get(1)).term();
        ensureIsEcho(op);
        assertEquals("echo(\"comment1234\")", op.toString());
    }

    protected void ensureIsEcho(Compound op) {
        assertEquals(Atom.the(echo.class.getSimpleName()),
                Operator.operatorName(op));
    }


    @Test public void testEmptyProduct() {
        Compound e = term("()");
        assertNotNull(e);
        assertEquals(0, e.size());

        Term o = term("<#x --> (/, ^Model_valid, T, (), _)>?");
        assertNotNull(o);
    }

//    @Test
//    public void testLineComment2() {
//        String a = "<a --> b>.\n'comment1234\n<b-->c>.";
//        List<Task> l = tasks(a);
//        assertEquals(3, l.size());
//        Operation op = ((Task<Operation>)l.get(1)).getTerm();
//        ensureIsEcho(op);
//        assertEquals("[\"comment1234\"]", op.argString());
//    }





    @Test
    public void testOperatorTerm() {
        Operator o = term("^op");
        assertNotNull(o);
        assertEquals("op", o.identifier().toString());
        assertEquals(Atom.class, o.identifier().getClass());
    }

    @Test
    public void testOperatorWithNoParams() {
        Compound t = term("op()");
        assertNotNull(t);
        assertEquals(0, Operator.opArgs(t).size());
    }

}
//class OldNarseseParser {
//
//    public final Memory memory;
//    private final NarseseParser newParser;
//    private Term self;
//
//
//
//
//    public OldNarseseParser(NAR n, NarseseParser newParser) {
//
//        this.memory = n.memory;
//        this.newParser = newParser;
//    }
//
//
//    /**
//     * Parse a line of addInput experience
//     * <p>
//     * called from ExperienceIO.loadLine
//     *
//     * @param buffer The line to be parsed
//     * @param memory Reference to the memory
//     * @param time The current time
//     * @return An experienced task
//     */
//    @Deprecated public Task parseNarsese(StringBuilder buffer) throws InvalidInputException {
//
//        throw new RuntimeException("use the new parser");
//
////        int i = buffer.indexOf(valueOf(PREFIX_MARK));
////        if (i > 0) {
////            String prefix = buffer.substring(0, i).trim();
////            if (prefix.equals(INPUT_LINE_PREFIX)) {
////                buffer.delete(0, i + 1);
////            } else if (prefix.equals(OUTPUT_LINE_PREFIX)) {
////                //ignore outputs
////                return null;
////            }
////        }
////
////        char c = buffer.charAt(buffer.length() - 1);
////        if (c == STAMP_CLOSER) {
////            //ignore stamp
////            int j = buffer.lastIndexOf(valueOf(STAMP_OPENER));
////            buffer.delete(j - 1, buffer.length());
////        }
////        c = buffer.charAt(buffer.length() - 1);
////        if (c == ']') {
////            int j = buffer.lastIndexOf(valueOf('['));
////            buffer.delete(j-1, buffer.length());
////        }
////
////        return parseTask(buffer.toString().trim());
//    }
//
//
////    public static Sentence parseOutput(String s) {
////        Term content = null;
////        char punc = 0;
////        TruthValue truth = null;
////
////        try {
////            StringBuilder buffer = new StringBuilder(s);
////            //String budgetString = getBudgetString(buffer);
////            String truthString = getTruthString(buffer);
////            String str = buffer.toString().trim();
////            int last = str.length() - 1;
////            punc = str.charAt(last);
////            //Stamp stamp = new Stamp(time);
////            truth = parseTruth(truthString, punc);
////
////
////            /*Term content = parseTerm(str.substring(0, last));
////            if (content == null) throw new InvalidInputException("Content term missing");*/
////        }
////        catch (InvalidInputException e) {
////            System.err.println("TextInput.parseOutput: " + s + " : " + e.toString());
////        }
////        return new Sentence(content, punc, truth, null);
////    }
//
//
////    public Task parseTask(String s) throws InvalidInputException {
////        return parseTask(s, true);
////    }
////
////    public Task parseTask(String s, boolean newStamp) throws InvalidInputException {
////        //ENTRY POINT TO NEW PARSER
////        return newParser.parseTask(s, newStamp);
////    }
//
//    public void parseTask(String s, Collection<? super Task> c) throws InvalidInputException {
//        newParser.tasks(n.memory, s, c);
//    }
//
//    public void parseTask(String s, Consumer<? super Task> c) throws InvalidInputException {
//        //ENTRY POINT TO NEW PARSER
//        newParser.tasks(n.memory, s, c);
//    }
//
//
////    public Task parseTaskIfEqualToOldParser(String s) throws InvalidInputException {
////
////        Task u = null, t = null;
////
////        InvalidInputException uError = null;
////        try {
////            u = parseTaskOld(s, true);
////        }
////        catch (InvalidInputException tt) {
////            uError = tt;
////        }
////
////
////        try {
////            t = parseTask(s, true);
////            if (t.equals(u))
////                return t;
////        }
////        catch (Throwable e) {
////            if (Global.DEBUG)
////                System.err.println("Task parse error: " + t + " isnt " + u + ": " + Arrays.toString(e.getStackTrace()));
////        }
////
////        if ((u == null) && (t!=null)) return t;
////        else {
////            if (uError!=null)
////                throw uError;
////        }
////
////        return u;
////
////    }
//
//
//    /**
//     * Enter a new Task in String into the memory, called from InputWindow or
//     * locally.
//     *
//     * @param s the single-line addInput String
//     * @param memory Reference to the memory
//     * @param time The current time
//     * @return An experienced task
//     */
//    public Task parseTaskOld(String s, boolean newStamp) throws InvalidInputException {
//        StringBuilder buffer = new StringBuilder(s);
//
//        String budgetString = getBudgetString(buffer);
//
//
//        Sentence sentence = parseSentenceOld(buffer, newStamp, Stamp.TIMELESS);
//        if (sentence == null) return null;
//
//        Budget budget = parseBudget(budgetString, sentence.getPunctuation(), sentence.getTruth());
//        Task task = new DefaultTask(sentence, budget, null, null);
//        return task;
//
//    }
//
////    public Sentence parseSentence(StringBuilder buffer) {
////        return parseSentence(buffer, true);
////    }
////
////    public Sentence parseSentence(StringBuilder buffer, boolean newStamp) {
////        return parseSentence(buffer, newStamp, Stamp.UNPERCEIVED);
////    }
////
//
//
//    public Sentence parseSentenceOld(StringBuilder buffer, boolean newStamp, long creationTime) {
//        String truthString = getTruthString(buffer);
//        Tense tense = parseTense(buffer);
//        String str = buffer.toString().trim();
//        if (str.isEmpty()) return null;
//        int last = str.length() - 1;
//        char punc = str.charAt(last);
//
//            /* if -1, will be set right before the Task is input */
//        //Stamper stamp = new Stamper(memory, creationTime, tense);
//
//        Truth truth = parseTruth(truthString, punc);
//        Term content = parseTerm(str.substring(0, last));
//        if (content == null) throw new InvalidInputException("Content term missing");
//        if (!(content instanceof Compound)) throw new InvalidInputException("Content term is not compound");
//
//        content = content.normalized();
//        if (content == null) return null;
//
//
//        return null;
////            Sentence s = new Sentence((Compound)content, punc, truth);
////            s.setCreationTime(creationTime);
////            s.setOccurrenceTime(tense, memory.duration());
////
////            //if ((content instanceof Conjunction) && Variable.containVarDep(content.getName())) {
////            //    sentence.setRevisible(false);
////            //}
////
////            return s;
//    }
//
//    /* ---------- react values ---------- */
//    /**
//     * Return the prefix of a task symbol that contains a BudgetValue
//     *
//     * @param s the addInput in a StringBuilder
//     * @return a String containing a BudgetValue
//     * @throws nars.io.StringParser.InvalidInputException if the addInput cannot be
//    parsed into a BudgetValue
//     */
//    private static String getBudgetString(StringBuilder s) throws InvalidInputException {
//        if (s.charAt(0) != BUDGET_VALUE_MARK) {
//            return null;
//        }
//        int i = s.indexOf(valueOf(BUDGET_VALUE_MARK), 1);    // looking for the end
//        if (i < 0) {
//            throw new InvalidInputException("missing budget closer");
//        }
//        String budgetString = s.substring(1, i).trim();
//        if (budgetString.isEmpty()) {
//            throw new InvalidInputException("empty budget");
//        }
//        s.delete(0, i + 1);
//        return budgetString;
//    }
//
//    /**
//     * Return the postfix of a task symbol that contains a TruthValue
//     *
//     * @return a String containing a TruthValue
//     * @param s the addInput in a StringBuilder
//     * @throws nars.io.StringParser.InvalidInputException if the addInput cannot be
//    parsed into a TruthValue
//     */
//    private static String getTruthString(final StringBuilder s) throws InvalidInputException {
//        final int last = s.length() - 1;
//        if (last==-1 || s.charAt(last) != TRUTH_VALUE_MARK) {       // use default
//            return null;
//        }
//        final int first = s.indexOf(valueOf(TRUTH_VALUE_MARK));    // looking for the beginning
//        if (first == last) { // no matching closer
//            throw new InvalidInputException("missing truth mark");
//        }
//        final String truthString = s.substring(first + 1, last).trim();
//        if (truthString.isEmpty()) {                // empty usage
//            throw new InvalidInputException("empty truth");
//        }
//        s.delete(first, last + 1);                 // remaining addInput to be processed outside
//        s.trimToSize();
//        return truthString;
//    }
//
//    /**
//     * react the addInput String into a TruthValue (or DesireValue)
//     *
//     * @param s addInput String
//     * @param type Task type
//     * @return the addInput TruthValue
//     */
//    private static Truth parseTruth(String s, char type) {
//        if ((type == QUESTION) || (type == QUEST)) {
//            return null;
//        }
//        float frequency = 1.0f;
//        float confidence = Global.DEFAULT_JUDGMENT_CONFIDENCE;
//        if (s != null) {
//            int i = s.indexOf(VALUE_SEPARATOR);
//            if (i < 0) {
//                frequency = parseFloat(s);
//            } else {
//                frequency = parseFloat(s.substring(0, i));
//                confidence = parseFloat(s.substring(i + 1));
//            }
//        }
//        return new DefaultTruth(frequency, confidence);
//    }
//
//    /**
//     * react the addInput String into a BudgetValue
//     *
//     * @param truth the TruthValue of the task
//     * @param s addInput String
//     * @param punctuation Task punctuation
//     * @return the addInput BudgetValue
//     * @throws nars.io.StringParser.InvalidInputException If the String cannot
//     * be parsed into a BudgetValue
//     */
//    private static Budget parseBudget(String s, char punctuation, Truth truth) throws InvalidInputException {
//        float priority, durability;
//        switch (punctuation) {
//            case JUDGMENT:
//                priority = Global.DEFAULT_JUDGMENT_PRIORITY;
//                durability = Global.DEFAULT_JUDGMENT_DURABILITY;
//                break;
//            case QUESTION:
//                priority = Global.DEFAULT_QUESTION_PRIORITY;
//                durability = Global.DEFAULT_QUESTION_DURABILITY;
//                break;
//            case GOAL:
//                priority = Global.DEFAULT_GOAL_PRIORITY;
//                durability = Global.DEFAULT_GOAL_DURABILITY;
//                break;
//            case QUEST:
//                priority = Global.DEFAULT_QUEST_PRIORITY;
//                durability = Global.DEFAULT_QUEST_DURABILITY;
//                break;
//            default:
//                throw new InvalidInputException("unknown punctuation: '" + punctuation + "'");
//        }
//        if (s != null) { // overrite default
//            int i = s.indexOf(VALUE_SEPARATOR);
//            if (i < 0) {        // default durability
//                priority = parseFloat(s);
//            } else {
//                int i2 = s.indexOf(VALUE_SEPARATOR, i+1);
//                if (i2 == -1)
//                    i2 = s.length();
//                priority = parseFloat(s.substring(0, i));
//                durability = parseFloat(s.substring(i + 1, i2));
//            }
//        }
//        float quality = (truth == null) ? 1 : truthToQuality(truth);
//        return new Budget(priority, durability, quality);
//    }
//
//    /**
//     * Recognize the tense of an addInput sentence
//     * @param s the addInput in a StringBuilder
//     * @return a tense value
//     */
//    public static Tense parseTense(StringBuilder s) {
//        int i = s.indexOf(Symbols.TENSE_MARK);
//        String t = "";
//        if (i > 0) {
//            t = s.substring(i).trim();
//            s.delete(i, s.length());
//        }
//        return Tense.tense(t);
//    }
//
//
//    public Term parseTerm(String s) throws InvalidInputException {
//        return newParser.termRaw(s);
//    }
//
//    /* ---------- react String into term ---------- */
//    /**
//     * Top-level method that react a Term in general, which may recursively call
//     itself.
//     * <p>
//     There are 5 valid cases: 1. (Op, A1, ..., An) is a CompoundTerm if Op is
//     a built-in getOperator 2. {A1, ..., An} is an SetExt; 3. [A1, ..., An] is an
//     SetInt; 4. <T1 Re T2> is a Statement (including higher-order Statement);
//     * 5. otherwise it is a simple term.
//     *
//     * @param s0 the String to be parsed
//     * @param memory Reference to the memory
//     * @return the Term generated from the String
//     */
//    public Term parseTermOld(String s) throws InvalidInputException {
//        throw new RuntimeException("deprecated");
////        s = s.trim();
////
////        if (s.length() == 0) return null;
////
////        int index = s.length() - 1;
////        char first = s.charAt(0);
////        char last = s.charAt(index);
////
////        NALOperator opener = getOpener(first);
////        if (opener!=null) {
////            switch (opener) {
////                case COMPOUND_TERM_OPENER:
////                    if (last == COMPOUND_TERM_CLOSER.ch) {
////                       return parsePossibleCompoundTermTerm(s.substring(1, index));
////                    } else {
////                        throw new InvalidInputException("missing CompoundTerm closer: " + s);
////                    }
////                case SET_EXT_OPENER:
////                    if (last == SET_EXT_CLOSER.ch) {
////                        return SetExt.make(parseArguments(s.substring(1, index) + ARGUMENT_SEPARATOR));
////                    } else {
////                        throw new InvalidInputException("missing ExtensionSet closer: " + s);
////                    }
////                case SET_INT_OPENER:
////                    if (last == SET_INT_CLOSER.ch) {
////                        return SetInt.make(parseArguments(s.substring(1, index) + ARGUMENT_SEPARATOR));
////                    } else {
////                        throw new InvalidInputException("missing IntensionSet closer: " + s);
////                    }
////                case STATEMENT_OPENER:
////                    if (last == STATEMENT_CLOSER.ch) {
////                        return parseStatement(s.substring(1, index));
////                    } else {
////                        throw new InvalidInputException("missing Statement closer: " + s);
////                    }
////            }
////        }
////        else if (Global.FUNCTIONAL_OPERATIONAL_FORMAT) {
////
////            //parse functional operation:
////            //  function()
////            //  function(a)
////            //  function(a,b)
////
////            //test for existence of matching parentheses at beginning at index!=0
////            int pOpen = s.indexOf('(');
////            int pClose = s.lastIndexOf(')');
////            if ((pOpen!=-1) && (pClose!=-1) && (pClose==s.length()-1)) {
////
////                String operatorString = Operator.addPrefixIfMissing(s.substring(0, pOpen));
////
////                Operator operator = memory.operator(operatorString);
////
////                if (operator == null) {
////                    //???
////                    throw new InvalidInputException("Unknown operate: " + operatorString);
////                }
////
////                String argString = s.substring(pOpen+1, pClose+1);
////
////
////                Term[] a;
////                if (argString.length() > 1) {
////                    ArrayList<Term> args = parseArguments(argString);
////                    a = args.toArray(new Term[args.size()]);
////                }
////                else {
////                    //void "()" arguments, default to (SELF)
////                    a = Terms.EmptyTermArray;
////                }
////
////                Operation o = Operation.make(operator, a, self);
////                return o;
////            }
////        }
////
////        //if no opener, parse the term
////        return parseAtomicTerm(s);
//
//    }
//
////    private static void showWarning(String message) {
////		new TemporaryFrame( message + "\n( the faulty line has been kept in the addInput window )",
////				40000, TemporaryFrame.WARNING );
////    }
//    /**
//     * Parse a Term that has no internal structure.
//     * <p>
//     * The Term can be a constant or a variable.
//     *
//     * @param s0 the String to be parsed
//     * @throws nars.io.StringParser.InvalidInputException the String cannot be
//     * parsed into a Term
//     * @return the Term generated from the String
//     */
//    private Term parseAtomicTerm(String s0) throws InvalidInputException {
//        String s = s0.trim();
//        if (s.isEmpty()) {
//            throw new InvalidInputException("missing term");
//        }
//
//
//
//        if (s.contains(" ")) { // invalid characters in a name
//            throw new InvalidInputException("invalid term");
//        }
//
//        char c = s.charAt(0);
//        if (c == Symbols.INTERVAL_PREFIX_OLD) {
//            return Interval.interval(s);
//        }
//
//
//        if (containVar(s)) {
//            return new Variable(s);
//        } else {
//            return Atom.the(s);
//        }
//    }
//
//    /**
//     * Check whether a string represent a name of a term that contains a
//     * variable
//     *
//     * @param n The string name to be checked
//     * @return Whether the name contains a variable
//     */
//    @Deprecated
//    public static boolean containVar(final CharSequence n) {
//        if (n == null) return false;
//        final int l = n.length();
//        for (int i = 0; i < l; i++) {
//            switch (n.charAt(i)) {
//                case Symbols.VAR_INDEPENDENT:
//                case Symbols.VAR_DEPENDENT:
//                case Symbols.VAR_QUERY:
//                    return true;
//            }
//        }
//        return false;
//    }
//
//
//
////        /**
////         * Parse a String to create a Statement.
////         *
////         * @return the Statement generated from the String
////         * @param s0 The addInput String to be parsed
////         * @throws nars.io.StringParser.InvalidInputException the String cannot be
////         * parsed into a Term
////         */
////        private Statement parseStatement(String s0) throws InvalidInputException {
////            String s = s0.trim();
////            int i = topRelation(s);
////            if (i < 0) {
////                throw new InvalidInputException("invalid statement: topRelation(s) < 0: " + s0);
////            }
////            String relation = s.substring(i, i + 3);
////            Term subject = parseTerm(s.substring(0, i));
////            Term predicate = parseTerm(s.substring(i + 3));
////            Statement t = make(getRelation(relation), subject, predicate, false, 0);
////            if (t == null) {
////                throw new InvalidInputException("invalid statement: statement unable to create: " + getOperator(relation) + " " + subject + " " + predicate);
////            }
////            return t;
////        }
//
//
//    public Compound parseCompoundTerm(String s) throws InvalidInputException {
//        Term t = parseTerm(s);
//        if (t instanceof Compound) return ((Compound)t);
//        throw new InvalidInputException(s + " is not a CompoundTerm");
//    }
//
////    /**
////     * Parse a String to create a CompoundTerm.
////     *
////     * @return the Term generated from the String
////     * @param s0 The String to be parsed
////     * @throws nars.io.StringParser.InvalidInputException the String cannot be
////     * parsed into a Term
////     */
////    public Term parsePossibleCompoundTermTerm(final String s0) throws InvalidInputException {
////        String s = s0.trim();
////        if (s.isEmpty()) {
////            throw new InvalidInputException("Empty compound term: " + s);
////        }
////        int firstSeparator = s.indexOf(ARGUMENT_SEPARATOR);
////        if (firstSeparator == -1) {
////            throw new InvalidInputException("Invalid compound term (missing ARGUMENT_SEPARATOR): " + s);
////        }
////
////        String op = (firstSeparator < 0) ? s : s.substring(0, firstSeparator).trim();
////        NALOperator oNative = getOperator(op);
////        Operator oRegistered = memory.operator(op);
////
////        if ((oRegistered==null) && (oNative == null)) {
////            throw new InvalidInputException("Unknown operate: " + op);
////        }
////
////        ArrayList<Term> arg = (firstSeparator < 0) ? new ArrayList<>(0)
////                : parseArguments(s.substring(firstSeparator + 1) + ARGUMENT_SEPARATOR);
////
////        Term[] argA = arg.toArray(new Term[arg.size()]);
////
////        Term t;
////
////        if (oNative!=null) {
////            t = Memory.term(oNative, argA);
////        }
////        else if (oRegistered!=null) {
////            t = Operation.make(oRegistered, argA, self);
////        }
////        else {
////            throw new InvalidInputException("Invalid compound term");
////        }
////
////        return t;
////    }
//
//    /**
//     * Parse a String into the argument get of a CompoundTerm.
//     *
//     * @return the arguments in an ArrayList
//     * @param s0 The String to be parsed
//     * @throws nars.io.StringParser.InvalidInputException the String cannot be
//     * parsed into an argument get
//     */
//    private ArrayList<Term> parseArguments(String s0) throws InvalidInputException {
//        String s = s0.trim();
//        ArrayList<Term> list = new ArrayList<>();
//        int start = 0;
//        int end = 0;
//        Term t;
//        while (end < s.length() - 1) {
//            end = nextSeparator(s, start);
//            if (end == start)
//                break;
//            t = parseTerm(s.substring(start, end));     // recursive call
//            list.add(t);
//            start = end + 1;
//        }
//        if (list.isEmpty()) {
//            throw new InvalidInputException("null argument");
//        }
//        return list;
//    }
//
//    /* ---------- locate top-level substring ---------- */
//    /**
//     * Locate the first top-level separator in a CompoundTerm
//     *
//     * @return the index of the next seperator in a String
//     * @param s The String to be parsed
//     * @param first The starting index
//     */
//    private static int nextSeparator(String s, int first) {
//        int levelCounter = 0;
//        int i = first;
//        while (i < s.length() - 1) {
//            if (isOpener(s, i)) {
//                levelCounter++;
//            } else if (isCloser(s, i)) {
//                levelCounter--;
//            } else if (s.charAt(i) == ARGUMENT_SEPARATOR) {
//                if (levelCounter == 0) {
//                    break;
//                }
//            }
//            i++;
//        }
//        return i;
//    }
//
////        /**
////         * locate the top-level getRelation in a statement
////         *
////         * @return the index of the top-level getRelation
////         * @param s The String to be parsed
////         */
////        private static int topRelation(final String s) {      // need efficiency improvement
////            int levelCounter = 0;
////            int i = 0;
////            while (i < s.length() - 3) {    // don't need to check the last 3 characters
////                if ((levelCounter == 0) && (isRelation(s.substring(i, i + 3)))) {
////                    return i;
////                }
////                if (isOpener(s, i)) {
////                    levelCounter++;
////                } else if (isCloser(s, i)) {
////                    levelCounter--;
////                }
////                i++;
////            }
////            return -1;
////        }
//
//    /* ---------- recognize symbols ---------- */
//    /**
//     * Check CompoundTerm opener symbol
//     *
//     * @return if the given String is an opener symbol
//     * @param s The String to be checked
//     * @param i The starting index
//     */
//    private static boolean isOpener(final String s, final int i) {
//        char c = s.charAt(i);
//
//        boolean b = (getOpener(c)!=null);
//        if (!b)
//            return false;
//
//        return i + 3 > s.length() || !isRelation(s.substring(i, i + 3));
//    }
//
//    /**
//     * Check CompoundTerm closer symbol
//     *
//     * @return if the given String is a closer symbol
//     * @param s The String to be checked
//     * @param i The starting index
//     */
//    private static boolean isCloser(String s, int i) {
//        char c = s.charAt(i);
//
//        boolean b = (getCloser(c)!=null);
//        if (!b)
//            return false;
//
//        return i < 2 || !isRelation(s.substring(i - 2, i + 1));
//    }
//
//    public static boolean possiblyNarsese(String s) {
//        return !s.contains("(") && !s.contains(")") && !s.contains("<") && !s.contains(">");
//    }
//
//
//    public void setSelf(Term arg) {
//        this.self = arg;
//    }
//
//    public Task parseOneTask(String taskText) {
//        List<Task> l = new ArrayList(1);
//        parseTask(taskText, l);
//        if (l.size() != 1) {
//            throw new RuntimeException("expected 1 task, got " + l.size());
//        }
//        return l.get(0);
//    }
//}
