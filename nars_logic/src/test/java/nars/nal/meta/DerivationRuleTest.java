package nars.nal.meta;

import junit.framework.TestCase;
import nars.$;
import nars.Narsese;
import nars.Op;
import nars.nal.RuleMatch;
import nars.nal.TaskRule;
import nars.term.Term;
import nars.term.Terms;
import nars.term.transform.FindSubst;
import nars.util.data.random.XorShift1024StarRandom;
import org.junit.Test;

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

    @Test public void testEllipsisMatch() {
        RuleMatch m = new RuleMatch(new XorShift1024StarRandom(1));
        FindSubst f = new FindSubst(Op.VAR_PATTERN, new XorShift1024StarRandom(2));
        Term p = $.$("(|, %1, %2..not(%1))");
        Term y = $.$("(|, x, y, z)");
        boolean r = f.match(p, y);
        System.out.println(f);
        System.out.println(r);


    }

}
