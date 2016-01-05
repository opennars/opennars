package nars.nal.meta;

import junit.framework.TestCase;
import nars.Narsese;
import nars.nal.PatternIndex;
import nars.nal.PremiseRule;
import nars.nal.PremiseRuleSet;
import nars.term.Term;
import nars.term.Terms;
import nars.term.compound.Compound;
import org.junit.Test;

/**
 * Created by me on 7/7/15.
 */
public class PremiseRuleTest extends TestCase {


    static final Narsese p = Narsese.the();

    /**
     * for printing complex terms as a recursive tree
     */
    public static void printRecursive(Term x) {
        Terms.printRecursive(x, 0);
    }


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
            PremiseRule x = p.termRaw("< A, A |- A, (Truth:Revision, Desire:Weak)>");
            assertEquals("((A,A),(A,(<Revision-->Truth>,<Weak-->Desire>)))", x.toString());
            // assertEquals(12, x.getVolume());
        }

        {
            PremiseRule x = (PremiseRule)p.term("< <A --> B>, <B --> A> |- <A <-> B>, (Truth:Revision, Desire:Weak)>");
            x = normalize(x);
            assertEquals(19, x.volume());
            assertEquals("((<%1-->%2>,<%2-->%1>),(<%1<->%2>,(<Revision-->Truth>,<Weak-->Desire>)))", x.toString());

        }
        {
            PremiseRule x = (PremiseRule)p.term("< <A --> B>, <B --> A> |- <A <-> nonvar>, (Truth:Revision, Desire:Weak)>");
            x = normalize(x);
            assertEquals(19, x.volume()); //same volume as previous block
            assertEquals("((<%1-->%2>,<%2-->%1>),(<nonvar<->%1>,(<Revision-->Truth>,<Weak-->Desire>)))", x.toString());
        }

        {
            PremiseRule x = (PremiseRule)p.term("< <A --> B>, <B --> A> |- <A <-> B>,  (Truth:Conversion, Punctuation:Judgment)>");
            x = normalize(x);
            assertEquals(19, x.volume());
            assertEquals("((<%1-->%2>,<%2-->%1>),(<%1<->%2>,(<Conversion-->Truth>,<Judgment-->Punctuation>)))", x.toString());
        }

//        {
//            TaskRule x = p.termRaw("<<A --> b> |- (X & y)>");
//            assertEquals("((<A --> b>), ((&, X, y)))", x.toString());
//            assertEquals(9, x.getVolume());
//        }

        //and the first complete rule:
        PremiseRule x = (PremiseRule) p.term("<(S --> M), (P --> M) |- (P <-> S), (Truth:Comparison,Desire:Strong)>");
        x = normalize(x);
        assertEquals("((<%1-->%2>,<%3-->%2>),(<%1<->%3>,(<Comparison-->Truth>,<Strong-->Desire>)))", x.toString());
        assertEquals(19, x.volume());

    }

    private PremiseRule normalize(PremiseRule x) {
        return new PremiseRuleSet(true, x).getFasterList().get(0);
    }

    @Test public void testNotSingleVariableRule1() {
        //tests an exceptional case that should now be fixed

        PatternIndex i = new PatternIndex();

        String l = "<((B,P) --> ?X) ,(B --> A), task(\"?\") |- ((B,P) --> (A,P)), (Truth:BeliefStructuralDeduction, Punctuation:Judgment)>";
        Compound x = ((PremiseRule)p.term(l)).normalizeRule(i);
        assertTrue(!x.toString().contains("%B"));
    }

    @Test
    public void testPatternVarNormalization() {

        Narsese p = Narsese.the();

        //TODO test combination of lowercase and uppercase pattern terms
//        TaskRule x = p.term("<<A --> b> |- (X & y)>");
//
//        assertEquals("((<%A --> b>), ((&, %X, y)))", x.toString());

        PatternIndex i = new PatternIndex();


        Compound y = (Compound)p.term("<(S --> P), --S |- (P --> S), (Truth:Conversion)>");
        y = ((PremiseRule)y).normalizeRule(i);
        printRecursive(y);

        assertEquals("((<%1-->%2>,(--,%1)),(<%2-->%1>,(<Conversion-->Truth>)))", y.toString());
        assertEquals(10, y.complexity());
        assertEquals(15, y.volume());
    }


    @Test public void printTermRecursive() {
        Compound y = (Compound)p.term("<(S --> P), --S |- (P --> S), (Truth:Conversion, Info:SeldomUseful)>");
        printRecursive(y);
    }








}
