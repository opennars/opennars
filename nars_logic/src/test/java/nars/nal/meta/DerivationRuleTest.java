package nars.nal.meta;

import junit.framework.TestCase;
import nars.$;
import nars.Global;
import nars.Narsese;
import nars.Op;
import nars.nal.TaskRule;
import nars.nal.nal4.Product;
import nars.term.*;
import nars.term.transform.FindSubst;
import nars.util.data.random.XorShift1024StarRandom;
import org.junit.Test;

import java.util.Set;

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
        TaskRule x = p.term(l);
        x = x.normalizeRule();
        assertTrue(!x.toString().contains("%B"));
    }

    @Test
    public void testPatternVarNormalization() {

        Narsese p = Narsese.the();

        //TODO test combination of lowercase and uppercase pattern terms
//        TaskRule x = p.term("<<A --> b> |- (X & y)>");
//
//        assertEquals("((<%A --> b>), ((&, %X, y)))", x.toString());



        TaskRule y = p.term("<(S --> P), --S |- (P --> S), (Truth:Conversion)>");
        y = y.normalizeRule();
        Terms.printRecursive(y);

        assertEquals("((<%1 --> %2>, (--,%1)), (<%2 --> %1>, (<Conversion --> Truth>)))", y.toString());
        assertEquals(10, y.complexity());
        assertEquals(15, y.volume());


    }


    @Test public void printTermRecursive() {
        TaskRule y = p.term("<(S --> P), --S |- (P --> S), (Truth:Conversion, Info:SeldomUseful)>");
        Terms.printRecursive(y);
    }

    @Test public void testEllipsis() {
        Narsese p = Narsese.the();
        String s = "%prefix..expression";
        Ellipsis t = p.term(s);
        assertNotNull(t);
        assertEquals(Ellipsis.class, t.getClass());
        assertEquals(s, t.toString());
        assertEquals("%prefix", t.name.toString());
        assertEquals("expression", t.expression.toString());

//        //multichar prefix
//        final Ellipsis abc0x = p.term("Abc:0..x");
//        assertEquals("Abc", abc0x.prefix);
//        assertEquals(0, abc0x.from);
//        assertEquals('x', abc0x.to);
    }

    @Test public void testEllipsisExpression() {
        //TODO
    }

    @Test public void testVarArg1() {
        String rule = "(%S --> %M), ((|, %S, %A..not(%S) ) --> %M) |- ((|, %A, ..) --> %M), (Truth:DecomposePositiveNegativeNegative)";
        TaskRule x = p.term("<" + rule + ">");
        System.out.println(x);
        x = x.normalizeRule();
        System.out.println(x);

//        //test with the automatically added '%' prefixes for capital letters:
//        String rule2 = "<(S --> M), ((|, S, A..not(S) ) --> M) |- ((|, A, ..) --> M), (Truth:DecomposePositiveNegativeNegative)>";
//        assertEquals( x, ((TaskRule)p.term(rule2)).normalizeRule() );

        assertEquals(
                "((<%1 --> %2>, <(|, %1, %3..not(%1)) --> %2>), (<(|, .., %3..not(%1)) --> %2>, (<DecomposePositiveNegativeNegative --> Truth>)))",
                x.toString());

        /*TrieDeriver d = new TrieDeriver(new DerivationRules(x));
        d.printSummary();*/

    }

    public interface EllipsisTest {
        Compound getPattern();
        Compound getResult();
        Compound getMatchable(int arity);

        default void test(int arity) {
            Set<Term> selectedFixed = Global.newHashSet(arity);

            Compound y = getMatchable(arity);
            Compound r = getResult();

            int repeats = 3; //large enough to ensure all combinations are produced

            Compound p = getPattern();

            for (int seed = 1; seed < arity * repeats /* enough chances to select all combinations */; seed++) {

                FindSubst f = new FindSubst(Op.VAR_PATTERN, new XorShift1024StarRandom(seed));

                boolean matched = f.next(p, y, 16);
                //System.out.println(f);
                assertTrue(matched);
                assertEquals(2, f.xy().size());

                Term varArgs = f.xy().get($.$("%2..not(%1)"));
                Term fixedTerm = f.xy().get($.$("%1"));


                assertEquals(Op.PRODUCT, varArgs.op());
                assertEquals(arity-1, varArgs.size());

                Set<Term> varArgTerms = Terms.toSortedSet(((Product) varArgs).terms());
                assertEquals(arity-1, varArgTerms.size());

                assertEquals(Atom.class, fixedTerm.getClass());
                assertFalse(varArgTerms.contains(fixedTerm));

                selectedFixed.add(fixedTerm);

                //2. test substitution
                Term s = r.substituted(f.xy());
                System.out.println(s);
                assertFalse(Variable.hasPatternVariable(s));
            }

            /** should have iterated all */
            assertEquals(arity, selectedFixed.size());

        }

        default void test(int arityMin, int arityMax) {
            for (int arity = arityMin; arity <= arityMax; arity++) {
                test(arity);
            }
        }
    }

    public static class Commutive1EllipsisTest implements EllipsisTest {
        private final String prefix, suffix;
        private final Compound p;

        public Commutive1EllipsisTest(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.p = $.$(prefix + "%1, %2..not(%1)" + suffix);
        }

        @Override
        public Compound getPattern() {
            return p;
        }

        @Override
        public Compound getResult() {
            return $.$("<" + prefix + "%2..not(%1), .." + suffix + " --> %1>");
        }

        @Override
        public Compound getMatchable(int arity) {
            return $.$(prefix + termSequence(arity) + suffix);
        }

    }

    @Test public void testEllipsisMatchCommutive1() {


        new Commutive1EllipsisTest("(|,", ")").test(2, 5);
        new Commutive1EllipsisTest("{", "}").test(2, 5);
        new Commutive1EllipsisTest("[", "]").test(2, 5);
        new Commutive1EllipsisTest("(&&,", ")").test(3, 5);


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

}
