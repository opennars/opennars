package nars.nal.meta;

import junit.framework.TestCase;
import nars.Narsese;
import nars.nal.TaskRule;
import nars.term.Terms;
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
            assertEquals("((<%A --> %B>, <%B --> %A>), (<%A <-> nonvar>, (<Revision --> Truth>, <Weak --> Desire>)))", x.toString());
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

    @Test public void testRangeTerm() {
        Narsese p = Narsese.the();
        Ellipsis t = p.term("A_1..n");
        assertNotNull(t);
        assertEquals(Ellipsis.class, t.getClass());
        assertEquals("A_1..n", t.toString());
        assertEquals("A", t.prefix);
        assertEquals(1, t.from);
        assertEquals('n', t.to);

        //multichar prefix
        final Ellipsis abc0x = p.term("Abc_0..x");
        assertEquals("Abc", abc0x.prefix);
        assertEquals(0, abc0x.from);
        assertEquals('x', abc0x.to);
    }


}
