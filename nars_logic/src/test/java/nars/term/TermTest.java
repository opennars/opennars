/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.term;

import nars.Global;
import nars.NAR;
import nars.Narsese;
import nars.concept.Concept;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.*;
import nars.nal.nal4.Image;
import nars.nal.nal8.Operation;
import nars.nar.Default;
import nars.nar.Terminal;
import nars.task.Task;
import org.junit.Test;

import java.util.TreeSet;

import static java.lang.Long.toBinaryString;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * @author me
 */
public class TermTest {
    static {
        Global.DEBUG = true;
    }

    NAR n = new Terminal();

    protected void assertEquivalent(String term1String, String term2String) {
        try {
            NAR n = new Default();

            Term term1 = n.term(term1String);
            Term term2 = n.term(term2String);

            assertTrue(term1 instanceof Compound);
            assertTrue(term2 instanceof Compound);
            assertNotEquals(term1String, term2String);

            assertEquals(term1, term2);
            assertEquals(term1, term2);
            assertEquals(0, term1.compareTo(term2));
        } catch (Exception e) {
            assertTrue(e.toString(), false);
        }
    }

    @Test
    public void testCommutativeCompoundTerm() throws Exception {
        NAR n = new Default();

        assertEquivalent("(&&,a,b)", "(&&,b,a)");
        assertEquivalent("(&&,(||,b,c),a)", "(&&,a,(||,b,c))");
        assertEquivalent("(&&,(||,c,b),a)", "(&&,a,(||,b,c))");

    }

    @Test
    public void testTermSort() throws Exception {
        NAR n = new Terminal();

        Term a = n.term("a");
        Term b = n.term("b");
        Term c = n.term("c");

        assertEquals(3, Terms.toSortedSetArray(a, b, c).length);
        assertEquals(2, Terms.toSortedSetArray(a, b, b).length);
        assertEquals(1, Terms.toSortedSetArray(a, a).length);
        assertEquals(1, Terms.toSortedSetArray(a).length);
        assertEquals("correct natural ordering", a, Terms.toSortedSetArray(a, b)[0]);
    }

    @Test
    public void testConjunctionTreeSet() throws Narsese.NarseseException {
        NAR n = new Default();


        //these 2 representations are equal, after natural ordering
        String term1String = "<#1 --> (&,boy,(/,taller_than,{Tom},_))>";
        Term term1 = n.term(term1String);
        String term1Alternate = "<#1 --> (&,(/,taller_than,{Tom},_),boy)>";
        Term term1a = n.term(term1Alternate);


        // <#1 --> (|,boy,(/,taller_than,{Tom},_))>
        Term term2 = n.term("<#1 --> (|,boy,(/,taller_than,{Tom},_))>");

        assertEquals(term1a.toString(), term1.toString());
        assertTrue(term1.complexity() > 1);
        assertTrue(term1.complexity() == term2.complexity());

        assertTrue(term1.getClass().equals(Inheritance.class));
        assertTrue(term1.getClass().equals(Inheritance.class));


        //System.out.println("t1: " + term1 + ", complexity=" + term1.getComplexity());
        //System.out.println("t2: " + term2 + ", complexity=" + term2.getComplexity());


        assertTrue(term1.equals(term1.clone()));
        assertTrue(term1.compareTo(term1.clone()) == 0);
        assertTrue(term2.equals(term2.clone()));
        assertTrue(term2.compareTo(term2.clone()) == 0);

        boolean t1e2 = term1.equals(term2);
        int t1c2 = term1.compareTo(term2);
        int t2c1 = term2.compareTo(term1);

        assertTrue(!t1e2);
        assertTrue("term1 and term2 inequal, so t1.compareTo(t2) should not = 0", t1c2 != 0);
        assertTrue("term1 and term2 inequal, so t2.compareTo(t1) should not = 0", t2c1 != 0);

        /*
        System.out.println("t1 equals t2 " + t1e2);
        System.out.println("t1 compareTo t2 " + t1c2);
        System.out.println("t2 compareTo t1 " + t2c1);
        */

        TreeSet<Term> set = new TreeSet<>();
        boolean added1 = set.add(term1.clone());
        boolean added2 = set.add(term2.clone());
        assertTrue("term 1 added to set", added1);
        assertTrue("term 2 added to set", added2);

        assertTrue(set.size() == 2);

    }

    @Test
    public void testUnconceptualizedTermInstancing() throws Narsese.NarseseException {
        NAR n = new Default();

        String term1String = "<a --> b>";
        Term term1 = n.term(term1String);
        Term term2 = n.term(term1String);

        assertTrue(term1.equals(term2));
        assertTrue(term1.hashCode() == term2.hashCode());

        Compound cterm1 = ((Compound) term1);
        Compound cterm2 = ((Compound) term2);

        //test subterms
        assertTrue(cterm1.term(0).equals(cterm2.term(0))); //'a'

    }

    @Test
    public void testConceptInstancing() throws Narsese.NarseseException {
        Default n = new Default();

        String statement1 = "<a --> b>.";

        Term a = n.term("a");
        assertTrue(a != null);
        Term a1 = n.term("a");
        assertTrue(a.equals(a1));

        n.input(statement1);
        n.frame(4);

        n.input(" <a  --> b>.  ");
        n.frame(1);
        n.input(" <a--> b>.  ");
        n.frame(1);

        String statement2 = "<a --> c>.";
        n.input(statement2);
        n.frame(4);

        Term a2 = n.term("a");
        assertTrue(a2 != null);

        Concept ca = n.concept(a2);
        assertTrue(ca != null);

        assertEquals(true, n.core.active.size() > 0);

    }

//    @Test
//    public void testEscaping() {
//        bidiEscape("c d", "x$# x", "\\\"sdkf sdfjk", "_ _");
//
////        NAR n = new Default().build();
////        n.addInput("<a --> \"b c\">.");
////        n.step(1);
////        n.finish(1);
////
////        Term t = new Term("\\\"b_c\\\"");
////        System.out.println(t);
////        System.out.println(n.memory.getConcepts());
////        System.out.println(n.memory.conceptProcessor.getConcepts());
////
////
////        assertTrue(n.memory.concept(new Term("a"))!=null);
////        assertTrue(n.memory.concept(t)!=null);
//
//    }

//    protected void bidiEscape(String... tests) {
//        for (String s : tests) {
//            s = '"' + s + '"';
//            String escaped = Texts.escape(s).toString();
//            String unescaped = Texts.unescape(escaped).toString();
//            //System.out.println(s + " " + escaped + " " + unescaped);
//            assertEquals(s, unescaped);
//        }
//    }

    @Test
    public void invalidTermIndep() {

        String t = "<$1-->(~,{place4},$1)>";
        NAR n = new Default();


        try {
            Task x = n.inputTask(t + ".");
            assertFalse(t + " is invalid compound term", true);
        } catch (Throwable tt) {
            assertTrue(true);
        }

        Term subj = null, pred = null;
        try {
            subj = n.term("$1");
            pred = n.term("(~,{place4},$1)");

            assertTrue(true);

        } catch (Throwable ex) {
            assertTrue(false);
        }


        Term s = Inheritance.make(subj, pred);
        assertEquals(null, s);


        Term i = Inheritance.make(subj, pred);
        assertEquals(null, i);


//        try {
            DefaultCompound2 forced = n.term("<a --> b>");
            assertNotNull(forced);

            forced.subterms().term[0] = subj;
            forced.subterms().term[1] = pred;
            forced.rehash();

            assertEquals(t, forced.toStringCompact());

            Term cloned = forced.clone();
            assertEquals(null, cloned);


//        } catch (Throwable ex) {
//            assertTrue(ex.toString(), false);
//        }
    }


    @Test
    public void testParseOperationInFunctionalForm() {

        NAR n = new Default();

        try {
            Term x = n.term("wonder(a,b)");
            assertEquals(Operation.class, x.getClass());
            assertEquals("wonder(a, b)", x.toString());

        } catch (Narsese.NarseseException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }


    }

    @Test
    public void testDifferenceImmediate() {

        Compound a = SetInt.make(Atom.the("a"), Atom.the("b"), Atom.the("c"));
        Compound b = SetInt.make(Atom.the("d"), Atom.the("b"));
        Term d = DifferenceInt.make(a, b);
        assertEquals(d.toString(), d.getClass(), SetIntN.class);
        assertEquals(d.toString(), 2, d.size());
        assertEquals("[a,c]", d.toString());
    }

    @Test
    public void testDifferenceImmediate2() {


        Compound a = SetExt.make(Atom.the("a"), Atom.the("b"), Atom.the("c"));
        Compound b = SetExt.make(Atom.the("d"), Atom.the("b"));
        Term d = DifferenceExt.make(a, b);
        assertEquals(d.toString(), d.getClass(), SetExtN.class);
        assertEquals(d.toString(), 2, d.size());
        assertEquals("{a,c}", d.toString());

    }

//    public void nullCachedName(String term) {
//        NAR n = new Default();
//        n.input(term + ".");
//        n.run(1);
//        assertNull("term name string was internally generated although it need not have been", ((Compound) n.concept(term).getTerm()).nameCached());
//    }
//
//    @Test public void avoidsNameConstructionUnlessOutputInheritance() {
//        nullCachedName("<a --> b>");
//    }
//
//    @Test public void avoidsNameConstructionUnlessOutputNegationAtomic() {
//        nullCachedName("(--, a)");
//    }
//    @Test public void avoidsNameConstructionUnlessOutputNegationCompound() {
//        nullCachedName("(--, <a-->b> )");
//    }
//
//    @Test public void avoidsNameConstructionUnlessOutputSetInt1() {
//        nullCachedName("[x]");
//    }
//    @Test public void avoidsNameConstructionUnlessOutputSetExt1() {
//        nullCachedName("{x}");
//    }

    @Test
    public void termEqualityWithQueryVariables() {
        NAR n = new Default();
        String a = "<?1-->bird>";
        assertEquals(n.term(a), n.term(a));
        String b = "<bird-->?1>";
        assertEquals(n.term(b), n.term(b));
    }

    protected void testTermEquality(String s) {

        Term a = n.term(s);
        Term b = n.term(s);
        assertTrue(a!=b);

        assertEquals(0, a.compareTo(b));
        assertEquals(0, b.compareTo(a));

        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.toString(), b.toString());

        assertEquals(a, b);

        assertEquals(a.normalized().toString(), b.toString());
        assertEquals(a.normalized().hashCode(), b.hashCode());
        assertEquals(a.normalized(), b);

    }

    @Test public void termEqualityOfVariables1() { testTermEquality("#1"); }
    @Test public void termEqualityOfVariables2() { testTermEquality("$1"); }
    @Test public void termEqualityOfVariables3() { testTermEquality("?1"); }
    @Test public void termEqualityOfVariables4() { testTermEquality("%1"); }


    @Test public void termEqualityWithVariables1() {
        testTermEquality("<#2 --> lock>");
    }

    @Test public void termEqualityWithVariables2() {
        testTermEquality("<<#2 --> lock> --> x>");
    }
    @Test public void termEqualityWithVariables3() {
        testTermEquality("(&&, x, <#2 --> lock>)");
        testTermEquality("(&&, x, <#1 --> lock>)");
    }
    @Test public void termEqualityWithVariables4() {
        testTermEquality("(&&, <<$1 --> key> ==> <#2 --> (/, open, $1, _)>>, <#2 --> lock>)");
    }

    @Test public void termEqualityWithMixedVariables() {
        NAR n = new Default();
        String s = "(&&, <<$1 --> key> ==> <#2 --> (/, open, $1, _)>>, <#2 --> lock>)";
        Term a = n.term(s);
        Term b = n.term(s);
        assertTrue(a!=b);
        assertEquals(a, b);
        b.equals(a.normalized());

        assertEquals("re-normalizing doesn't affect: " + a.normalized(), b,
                a.normalized() );

    }
    @Test
    public void validStatement() {
        NAR n = new Default();
        Term t = n.term("<(*,{tom},{vienna}) --> livingIn>");
        assertFalse(Statement.invalidStatement((Inheritance) t));
    }

    @Test
    public void statementHash() {
        //this is a case where a faulty hash function produced a collision
        statementHash("i4", "i2");
        statementHash("{i4}", "{i2}");
        statementHash("<{i4} --> r>", "<{i2} --> r>");


        statementHash("<<{i4} --> r> ==> A(7)>", "<<{i2} --> r> ==> A(8)>");

        statementHash("<<{i4} --> r> ==> A(7)>", "<<{i2} --> r> ==> A(7)>");

    }

    @Test
    public void statementHash2() {
        statementHash("<<{i4} --> r> ==> A(7)>", "<<{i2} --> r> ==> A(9)>");
    }

    @Test
    public void statementHash3() {

        //this is a case where a faulty hash function produced a collision
        statementHash("<<{i0} --> r> ==> A(8)>", "<<{i1} --> r> ==> A(7)>");

        //this is a case where a faulty hash function produced a collision
        statementHash("<<{i10} --> r> ==> A(1)>", "<<{i11} --> r> ==> A(0)>");
    }

    public void statementHash(String a, String b) {

        NAR n = new Default();

        Term ta = n.term(a);
        Term tb = n.term(b);

        assertNotEquals(ta, tb);
        assertNotEquals(ta.toString() + " vs. " + tb.toString(),
                ta.hashCode(), tb.hashCode());


    }

    @Test
    public void testTermComplexityMass() {
        NAR n = new Default();

        testTermComplexityMass(n, "x", 1, 1);

        testTermComplexityMass(n, "#x", 0, 1, 0, 1, 0);
        testTermComplexityMass(n, "$x", 0, 1, 1, 0, 0);
        testTermComplexityMass(n, "?x", 0, 1, 0, 0, 1);

        testTermComplexityMass(n, "<a --> b>", 3, 3);
        testTermComplexityMass(n, "<#a --> b>", 2, 3, 0, 1, 0);

        testTermComplexityMass(n, "<a --> (c & d)>", 5, 5);
        testTermComplexityMass(n, "<$a --> (c & #d)>", 3, 5, 1, 1, 0);
    }

    private void testTermComplexityMass(NAR n, String x, int complexity, int mass) {
        testTermComplexityMass(n, x, complexity, mass, 0, 0, 0);
    }

    private void testTermComplexityMass(NAR n, String x, int complexity, int mass, int varIndep, int varDep, int varQuery) {
        Term t = n.term(x);

        assertNotNull(t);
        assertEquals(complexity, t.complexity());
        assertEquals(mass, t.volume());

        assertEquals(varDep, t.varDep());
        assertEquals(varDep != 0, t.hasVarDep());

        assertEquals(varIndep, t.varIndep());
        assertEquals(varIndep != 0, t.hasVarIndep());

        assertEquals(varQuery, t.varQuery());
        assertEquals(varQuery != 0, t.hasVarQuery());

        assertEquals(varDep + varIndep + varQuery, t.vars());
        assertEquals((varDep + varIndep + varQuery) != 0, t.hasVar());
    }

    public <C extends Compound> C testStructure(String term, String bits) {
        C a = n.term(term);
        assertEquals(term, a.toString(true));
        assertEquals(bits, toBinaryString(a.structure()));
        return a;
    }

    @Test
    public void testSubtermsVector() {

        NAR n = new Default();

        Term a3 = n.term("c");

        Compound a = testStructure( "<<a --> b> </> c>",  "100000000000000000001000001");
        Compound a1 = testStructure("<c <|> <a --> b>>", "1000000000000000000001000001");
        Compound a2 = testStructure("<c <=> <a --> b>>",   "10000000000000000001000001");
        Compound b = testStructure("<<$1 --> #2> </> ?3>", "100000000000000000001001110");

        assertTrue( a.impossibleToMatch(b) );
        assertFalse( a.impossibleToMatch(a3));


        assertEquals("no additional structure code in upper bits",
                 a.structure(), a.structure());
        assertEquals("no additional structure code in upper bits",
                b.structure(), b.structure());


    }


    @Test
    public void testImageOrdering1() {
        testImageOrdering('/');
    }
    @Test
    public void testImageOrdering2() {
        testImageOrdering('\\');
    }

    void testImageOrdering(char v) {

        Image a = n.term("(" + v + ",x, y, _)");
        Image b = n.term("(" + v + ",x, _, y)");
        Image c = n.term("(" + v + ",_, x, y)");
        assertNotEquals(a.relationIndex, b.relationIndex);
        assertNotEquals(b.relationIndex, c.relationIndex);

        assertNotEquals(a, b);
        assertNotEquals(b, c);
        assertNotEquals(a, c);

        assertNotEquals(a.hashCode(), b.hashCode());
        assertNotEquals(b.hashCode(), c.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());

        assertEquals(-1, a.compareTo(b));
        assertEquals(+1, b.compareTo(a));

        assertEquals(-1, a.compareTo(c));
        assertEquals(+1, c.compareTo(a));

        assertEquals(-1, b.compareTo(c));
        assertEquals(+1, c.compareTo(b));


    }

    @Test
    public void testImageStructuralVector() {

        String i1 = "(/, x, y, _)";
        String i2 = "(/, x, _, y)";
        Image a = testStructure(i1,  "10000000000001");
        Image b = testStructure(i2,  "10000000000001");

        /*assertNotEquals("additional structure code in upper bits",
                a.structure2(), b.structure2());*/
        assertNotEquals(a.relationIndex, b.relationIndex);
        assertNotEquals("structure code influenced contentHash",
                b.hashCode(), a.hashCode());

        NAR n = new Terminal();
        Compound x3 = n.term("<" + i1 + " --> y>");
        Compound x4 = n.term("<" + i1 + " --> y>");

        assertFalse("i2 is a possible subterm of x3, structurally, even if the upper bits differ",
                x3.impossibleSubTermOrEquality(n.term(i2)));
        assertFalse(
                x4.impossibleSubTermOrEquality(n.term(i1)));


    }

    @Test public void testOperationArguments() {
        NAR n = new Default();

        assertNotNull( Operation.getArgumentProduct(n.term("{(a)}")) );
        assertNotNull( Operation.getArgumentProduct(n.term("{(a,b)}")) );

        try {
            Operation.getArgumentProduct(n.term("(a,b)"));
            assertTrue(false); //should throw exception
        }
        catch (Exception e) {  }

        try {
            assertNull( Operation.getArgumentProduct(n.term("({a})")) );
            assertTrue(false); //should throw exception
        }
        catch (Exception e) {  }

    }

    @Test public void testSubTermStructure() {

        assertTrue(
                n.term("<a --> b>").impossibleSubterm(
                        n.term("<a-->b>")
                )
        );
        assertTrue(
                n.term("<a --> b>").impossibleToMatch(
                        n.term("<a-->#b>")
                )
        );

    }

    @Test public void testCommutativeWithVariableEquality() {
        Term a = n.term("<(&&, <#1 --> M>, <#2 --> M>) ==> <#2 --> nonsense>>");
        Term b = n.term("<(&&, <#2 --> M>, <#1 --> M>) ==> <#2 --> nonsense>>");
        assertEquals(a, b);

        Term c = n.term("<(&&, <#1 --> M>, <#2 --> M>) ==> <#1 --> nonsense>>");
        assertNotEquals(a, c);

        Compound x= n.term("(&&, <#1 --> M>, <#2 --> M>)");
        Term xa = x.term(0);
        Term xb = x.term(1);
        int o1 = xa.compareTo(xb);
        int o2 = xb.compareTo(xa);
        assertEquals(o1, -o2);
        assertNotEquals(0, o1);
        assertNotEquals(xa, xb);
    }

    @Test public void testHash1() {
        testUniqueHash("<A --> B>","<A <-> B>");
        testUniqueHash("<A --> B>","<A ==> B>");
        testUniqueHash("A","B");
        testUniqueHash("%1","%2");
        testUniqueHash("%A","A");
        testUniqueHash("$1","A");
        testUniqueHash("$1","#1");
    }

    public void testUniqueHash(String a, String b) {
        int h1 = n.term(a).hashCode();
        int h2 = n.term(b).hashCode();
        assertNotEquals(h1, h2);
    }
}
