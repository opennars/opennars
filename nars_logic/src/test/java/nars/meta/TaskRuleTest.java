package nars.meta;

import junit.framework.TestCase;
import nars.nal.NALExecuter;
import nars.narsese.NarseseParser;
import org.junit.Test;

/**
 * Created by me on 7/7/15.
 */
public class TaskRuleTest extends TestCase {

    @Test
    public void testParser() {

        NALExecuter executer =new NALExecuter(); //all the inference rules have to pass of course

        NarseseParser p = NarseseParser.the();
        //NAR p = new NAR(new Default());

        assertNotNull("metaparser can is a superset of narsese", p.term("<A --> b>"));

        //

        {
            TaskRule x = p.term("< A, A |- A, (Truth_Revision, Desire_Weak)>");
            assertEquals("((A, A), (A, (Truth_Revision, Desire_Weak)))", x.toString());
            // assertEquals(12, x.getVolume());
        }

        {
            TaskRule x = p.term("< <A --> B>, <B --> A> |- <A <-> B>>");
            assertEquals("((<A --> B>, <B --> A>), (<A <-> B>))", x.toString());
            assertEquals(12, x.getVolume());
        }

        {
            TaskRule x = p.term("< <A --> B>, <B --> A> |- <A <-> B>, <Nonsense --> Test>>");
            assertEquals("((<A --> B>, <B --> A>), (<A <-> B>, <Nonsense --> Test>))", x.toString());
            assertEquals(15, x.getVolume());
        }

        {
            TaskRule x = p.term("<<A --> b> |- (X & y)>");
            assertEquals("((<A --> b>), ((&, X, y)))", x.toString());
            assertEquals(9, x.getVolume());
        }

        {
            //and the first complete rule:
            TaskRule x = p.term("<(S --> M), (P --> M) |- (P <-> S), (TruthComparison,DesireStrong)>");
            assertEquals("((<S --> M>, <P --> M>), (<P <-> S>, (TruthComparison, DesireStrong)))", x.toString());
            assertEquals(15, x.getVolume());
        }

        //TODO test that Pattern Variables are created for uppercase atoms


    }

    @Test
    public void testPatternVariables() {

        NarseseParser p = NarseseParser.the();
        TaskRule x = p.term("<<A --> b> |- (X & y)>");
        assertEquals("((<A --> b>), ((&, X, y)))", x.toString());

        x.normalize();

        assertEquals("((<%A --> b>), ((&, %X, y)))", x.toString());


    }

}
