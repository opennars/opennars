package nars.meta;

import junit.framework.TestCase;
import nars.narsese.NarseseParser;
import nars.term.Term;
import org.junit.Test;

/**
 * Created by me on 7/7/15.
 */
public class TaskRuleTest extends TestCase {

    @Test
    public void testParser() {

        NarseseParser p = NarseseParser.the();
        //NAR p = new NAR(new Default());

        assertNotNull("metaparser can is a superset of narsese", p.term("<A --> b>"));

        {
            TaskRule x = p.term("< <A --> B>, <B --> A> |- <A <-> B>>");
            assertEquals("((<A --> B>, <B --> A>), (<A <-> B>))", x.toString());
            assertEquals(12, x.getMass());
        }

        {
            TaskRule x = p.term("< <A --> B>, <B --> A> |- <A <-> B>, <Nonsense --> Test>>");
            assertEquals("((<A --> B>, <B --> A>), (<A <-> B>, <Nonsense --> Test>))", x.toString());
            assertEquals(15, x.getMass());
        }

        {
            TaskRule x = p.term("<<A --> b> |- (X & y)>");
            assertEquals("((<A --> b>), ((&, X, y)))", x.toString());
            assertEquals(9, x.getMass());
        }

        {
            //and the first complete rule:
            TaskRule x = p.term("<(S --> M), (P --> M) |- (P <-> S), (TruthComparison,DesireStrong)>");
            assertEquals("((<S --> M>, <P --> M>), (<P <-> S>, (TruthComparison, DesireStrong)))", x.toString());
            assertEquals(15, x.getMass());
        }

        //TODO test that Pattern Variables are created for uppercase atoms


    }

}