package nars.nal.meta;

import junit.framework.TestCase;
import nars.$;
import nars.Global;
import nars.Narsese;
import nars.Op;
import nars.nal.TaskRule;
import nars.nal.meta.match.*;
import nars.term.Term;
import nars.term.Terms;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.term.transform.FindSubst;
import nars.term.variable.Variable;
import nars.util.data.random.XorShift1024StarRandom;
import org.junit.Test;

import java.util.Random;
import java.util.Set;

import static nars.$.$;

/**
 * Created by me on 7/7/15.
 */
public class DerivationRuleTest extends TestCase {


    static final Narsese p = Narsese.the();

 

    @Test
    public void testParser() {



        //NAR p = new NAR(new Default());

        assertNotNull("metaparser can is a superset of narsese", p.termRaw("<A --> b>"));

        //

        assertEquals(0, p.term("#A").complexity());
        assertEquals(1, p.term("#A").volume());
        assertEquals(0, p.term("%A").complexity());
        assertEquals(1, p.term("%A").volume());

        assertEquals(3, p.term("<A --> B>").complexity());
        assertEquals(1, p.term("<%A --> %B>").complexity());

        {
            TaskRule x = p.termRaw("< A, A |- A, (Truth:Revision, Desire:Weak)>");
            assertEquals("((A, A), (A, (<Revision --> Truth>, <Weak --> Desire>)))", x.toString());
            // assertEquals(12, x.getVolume());
        }

        {
            TaskRule x = p.term("< <A --> B>, <B --> A> |- <A <-> B>, (Truth:Revision, Desire:Weak)>");
            assertEquals(19, x.volume());
            assertEquals("((<%A --> %B>, <%B --> %A>), (<%A <-> %B>, (<Revision --> Truth>, <Weak --> Desire>)))", x.toString());
        }
        {
            TaskRule x = p.term("< <A --> B>, <B --> A> |- <A <-> nonvar>, (Truth:Revision, Desire:Weak)>");
            assertEquals(19, x.volume()); //same volume as previous block
            assertEquals("((<%A --> %B>, <%B --> %A>), (<nonvar <-> %A>, (<Revision --> Truth>, <Weak --> Desire>)))", x.toString());
        }

        {
            TaskRule x = p.term("< <A --> B>, <B --> A> |- <A <-> B>, (<Nonsense --> Test>)>");
            assertEquals(16, x.volume());
            assertEquals("((<%A --> %B>, <%B --> %A>), (<%A <-> %B>, (<%Nonsense --> %Test>)))", x.toString());
        }

//        {
//            TaskRule x = p.termRaw("<<A --> b> |- (X & y)>");
//            assertEquals("((<A --> b>), ((&, X, y)))", x.toString());
//            assertEquals(9, x.getVolume());
//        }

        {
            //and the first complete rule:
            TaskRule x = p.term("<(S --> M), (P --> M) |- (P <-> S), (TruthComparison,DesireStrong)>");
            assertEquals("((<%S --> %M>, <%P --> %M>), (<%P <-> %S>, (%TruthComparison, %DesireStrong)))", x.toString());
            assertEquals(15, x.volume());
        }



    }

    @Test public void testNotSingleVariableRule1() {
        //tests an exceptional case that should now be fixed

        String l = "<((B,P) --> ?X) ,(B --> A), task(\"?\") |- ((B,P) --> (A,P)), (Truth:BeliefStructuralDeduction, Punctuation:Judgment)>";
        Compound x = ((TaskRule)p.term(l)).normalizeRule();
        assertTrue(!x.toString().contains("%B"));
    }

    @Test
    public void testPatternVarNormalization() {

        Narsese p = Narsese.the();

        //TODO test combination of lowercase and uppercase pattern terms
//        TaskRule x = p.term("<<A --> b> |- (X & y)>");
//
//        assertEquals("((<%A --> b>), ((&, %X, y)))", x.toString());



        Compound y = p.term("<(S --> P), --S |- (P --> S), (Truth:Conversion)>");
        y = ((TaskRule)y).normalizeRule();
        Terms.printRecursive(y);

        assertEquals("((<%1 --> %2>, (--,%1)), (<%2 --> %1>, (<Conversion --> Truth>)))", y.toString());
        assertEquals(10, y.complexity());
        assertEquals(15, y.volume());


    }


    @Test public void printTermRecursive() {
        Compound y = p.term("<(S --> P), --S |- (P --> S), (Truth:Conversion, Info:SeldomUseful)>");
        Terms.printRecursive(y);
    }

    @Test public void testEllipsisOneOrMore() {
        String s = "%prefix..+";
        Ellipsis t = $(s);
        assertNotNull(t);
        assertEquals(s, t.toString());
        assertEquals("%prefix", t.target.toString());
        assertEquals(EllipsisOneOrMore.class, t.getClass());

        assertEquals(t.target, $("%prefix")); //equality between target and itself
    }

    @Test public void testEllipsisZeroOrMore() {
        String s = "%prefix..*";
        Ellipsis t = $(s);
        assertNotNull(t);
        assertEquals(s, t.toString());
        assertEquals("%prefix", t.target.toString());
        assertEquals(EllipsisZeroOrMore.class, t.getClass());
    }
    @Test public void testEllipsisTransform() {
        String s = "%A..%B=C..+";
        EllipsisTransform t = $(s);
        assertNotNull(t);
        assertEquals(s, t.toString());
        assertEquals(EllipsisTransform.class, t.getClass());
        assertEquals($("%B"), t.from);
        assertEquals($("C"), t.to);

        Term u = new TaskRule.TaskRuleVariableNormalization($.p(t)).get();
        t = (EllipsisTransform)((Compound)u).term(0);
        assertEquals("(%1..%2=C..+)", u.toString());
        assertEquals($("%2"), t.from);
        assertEquals($("C"), t.to);
    }

    @Test public void testEllipsisExpression() {
        //TODO
    }

    @Test public void testVarArg0() {
        //String rule = "(%S --> %M), ((|, %S, %A..+ ) --> %M) |- ((|, %A, ..) --> %M), (Truth:DecomposePositiveNegativeNegative)";
        String rule = "(%S ==> %M), ((&&,%S,%A..+) ==> %M) |- ((&&,%A,..) ==> %M), (Truth:DecomposeNegativePositivePositive, Order:ForAllSame, SequenceIntervals:FromBelief)";
        Compound x = p.term("<" + rule + ">");
        //System.out.println(x);
        x = ((TaskRule)x).normalizeRule();
        //System.out.println(x);

        assertEquals(
            "((<%1 ==> %2>, <(&&, %1, %3..+) ==> %2>), (<(&&, %3..+, ..) ==> %2>, (<DecomposeNegativePositivePositive --> Truth>, <ForAllSame --> Order>, <FromBelief --> SequenceIntervals>)))",
            x.toString()
        );

    }



    public interface EllipsisTest {
        Compound getPattern();
        Compound getResult();
        String getEllipsis();
        Compound getMatchable(int arity);

        default Set<Term> test(int arity, int repeats) {
            Set<Term> selectedFixed = Global.newHashSet(arity);

            Compound y = getMatchable(arity);
            Compound r = getResult();

            Compound x = getPattern();

            final Term ellipsisTerm = $(getEllipsis());

            for (int seed = 0; seed < Math.max(1,repeats*arity) /* enough chances to select all combinations */; seed++) {

                FindSubst f = new FindSubst(Op.VAR_PATTERN, new XorShift1024StarRandom(1+seed));

                boolean matched = f.next(x, y, 16);
                //System.out.println(x + "\t" + y + "\t" +f);
                assertTrue(matched);

                EllipsisMatch varArgs = (EllipsisMatch)f.getXY(ellipsisTerm);


                assertEquals(getExpectedUniqueTerms(arity), varArgs.size());

                Set<Term> varArgTerms = Global.newHashSet(1);
                varArgs.applyTo(f, varArgTerms, true);

                assertEquals(getExpectedUniqueTerms(arity), varArgTerms.size());

                testFurther(selectedFixed, f, varArgTerms);

                //2. test substitution
                Term s = r.apply(f, false);
                System.out.println(s);

                selectedFixed.add(s);


                assertFalse(Variable.hasPatternVariable(s));
            }


            return selectedFixed;

        }

        int getExpectedUniqueTerms(int arity);

        default void testFurther(Set<Term> selectedFixed, FindSubst f, Set<Term> varArgTerms) {

        }

        default void test(int arityMin, int arityMax, int repeats) {
            for (int arity = arityMin; arity <= arityMax; arity++) {
                test(arity, repeats);
            }
        }
    }

    public abstract static class CommutiveEllipsisTest implements EllipsisTest {
        protected final String prefix;
        protected final String suffix;
        protected final Compound p;
        private final String ellipsis;

        public CommutiveEllipsisTest(String ellipsisTerm, String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.ellipsis = ellipsisTerm;
            this.p = getPattern(prefix, suffix);
        }

        protected abstract Compound getPattern(String prefix, String suffix);


        @Override public String getEllipsis() { return ellipsis; }

        @Override
        public Compound getPattern() {
            return p;
        }


        @Override
        public Compound getMatchable(int arity) {
            return $(prefix + DerivationRuleTest.termSequence(arity) + suffix);
        }
    }

    public static class CommutiveEllipsisTest1 extends CommutiveEllipsisTest {

        static final Term fixedTerm = $("%1");

        public CommutiveEllipsisTest1(String ellipsisTerm, String[] openClose) {
            super(ellipsisTerm, openClose[0], openClose[1]);
        }

        @Override
        public Set<Term> test(int arity, int repeats) {
            Set<Term> selectedFixed = super.test(arity, repeats);

            /** should have iterated all */
            assertEquals(arity, selectedFixed.size());
            return selectedFixed;
        }

        @Override
        public int getExpectedUniqueTerms(int arity) {
            return arity-1;
        }

        @Override public void testFurther(Set<Term> selectedFixed, FindSubst f, Set<Term> varArgTerms) {
            assertEquals(f.toString(), 2, f.xy.size());
            Term fixedTermValue = f.getXY(fixedTerm);
            assertEquals(Atom.class, fixedTermValue.getClass());
            assertFalse(varArgTerms.contains(fixedTermValue));
        }


        public Compound getPattern(String prefix, String suffix) {
            return $(prefix + "%1, " + getEllipsis() + suffix);
        }

        @Override
        public Compound getResult() {
            return $("<" + prefix + getEllipsis() + ", .." + suffix + " --> %1>");
        }

    }

    /** for testing zero-or-more matcher */
    public static class CommutiveEllipsisTest2 extends CommutiveEllipsisTest {

        public CommutiveEllipsisTest2(String ellipsisTerm, String[] openClose) {
            super(ellipsisTerm, openClose[0], openClose[1]);
        }

        @Override
        public Set<Term> test(int arity, int repeats) {
            Set<Term> s = super.test(arity, repeats);
            Term the = s.iterator().next();
            assertTrue( the.toString().substring(1).startsWith("Z"));
            return s;
        }

        public Compound getPattern(String prefix, String suffix) {
            return $(prefix + getEllipsis() + suffix);
        }



        @Override
        public Compound getResult() {
            return $(prefix + "Z, " + getEllipsis() + ", .." + suffix);
        }

        @Override
        public int getExpectedUniqueTerms(int arity) {
            return arity;
        }
    }


    public static String[] p(String a, String b) { return new String[] { a, b}; };


    @Test public void testEllipsisMatchCommutive1() {
        for (String e : new String[]{"%2..+"}) {
            for (String[] s : new String[][]{p("(|,", ")"), p("{", "}"), p("[", "]"), p("(&&,", ")")}) {
                new CommutiveEllipsisTest1(e, s).test(2, 5, 4);
            }
        }
    }
    @Test public void testEllipsisMatchCommutive2() {
        for (String e : new String[] { "%1..+" }) {
            for (String[] s : new String[][] { p("{", "}"), p("[", "]"), p("(", ")") }) {
                new CommutiveEllipsisTest2(e, s).test(1, 5, 0);
            }
        }
    }
    @Test public void testEllipsisMatchCommutive2_empty() {
        for (String e : new String[] { "%1..*" }) {
            for (String[] s : new String[][] { p("(", ")") }) {
                new CommutiveEllipsisTest2(e, s).test(0, 2, 0);
            }
        }
    }

    static String termSequence(int arity) {
        StringBuilder sb = new StringBuilder(arity * 3);
        for (int i = 0; i < arity; i++) {
            sb.append( (char)('a' + i) );
            if (i < arity-1)
                sb.append(',');
        }
        return sb.toString();
    }


    public void testEllipsisCombinatorics1() {

        //rule: ((&&,M,A..+) ==> C), ((&&,A,..) ==> C) |- M, (Truth:Abduction, Order:ForAllSame)
        Compound X = $("(&&, %1..+, %2)");
        Compound Y = $("(&&, <r --> [c]>, <r --> [w]>, <r --> [f]>)");
        int expect = 3;

        Set<String> results = Global.newHashSet(0);
        for (int seed = 0; seed < expect*5; seed++) {
            Random rng = new XorShift1024StarRandom(seed);
            FindSubst f = new FindSubst(Op.VAR_PATTERN, rng);
            f.setPower(1000);
            boolean b = f.match(X, Y);
            assertTrue(b);
            results.add(f.xy.toString());
        }

        results.forEach(System.out::println);
        assertEquals(expect, results.size());




    }
}
