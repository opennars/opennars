package nars.meta;

import junit.framework.TestCase;
import nars.meter.NARComparator;
import nars.nal.NALExecuter;
import nars.nar.Default;
import nars.nar.NewDefault;
import nars.narsese.NarseseParser;
import org.junit.Test;

/**
 * Created by me on 7/7/15.
 */
public class TaskRuleTest extends TestCase {

    static final NALExecuter executer = NALExecuter.defaults; //all the inference rules have to pass of course

    @Test
    public void testParser() {


        NarseseParser p = NarseseParser.the();
        //NAR p = new NAR(new Default());

        assertNotNull("metaparser can is a superset of narsese", p.termRaw("<A --> b>"));

        //

        {
            TaskRule x = p.termRaw("< A, A |- A, (Truth_Revision, Desire_Weak)>");
            assertEquals("((A, A), (A, (Truth_Revision, Desire_Weak)))", x.toString());
            // assertEquals(12, x.getVolume());
        }

        {
            TaskRule x = p.termRaw("< <A --> B>, <B --> A> |- <A <-> B>>");
            assertEquals("((<A --> B>, <B --> A>), (<A <-> B>))", x.toString());
            assertEquals(12, x.getVolume());
        }

        {
            TaskRule x = p.termRaw("< <A --> B>, <B --> A> |- <A <-> B>, <Nonsense --> Test>>");
            assertEquals("((<A --> B>, <B --> A>), (<A <-> B>, <Nonsense --> Test>))", x.toString());
            assertEquals(15, x.getVolume());
        }

        {
            TaskRule x = p.termRaw("<<A --> b> |- (X & y)>");
            assertEquals("((<A --> b>), ((&, X, y)))", x.toString());
            assertEquals(9, x.getVolume());
        }

        {
            //and the first complete rule:
            TaskRule x = p.termRaw("<(S --> M), (P --> M) |- (P <-> S), (TruthComparison,DesireStrong)>");
            assertEquals("((<S --> M>, <P --> M>), (<P <-> S>, (TruthComparison, DesireStrong)))", x.toString());
            assertEquals(15, x.getVolume());
        }

        //TODO test that Pattern Variables are created for uppercase atoms


    }

    @Test
    public void testPatternVariables() {

        NarseseParser p = NarseseParser.the();
        TaskRule x = p.term("<<A --> b> |- (X & y)>");

        assertEquals("((<%A --> b>), ((&, %X, y)))", x.toString());


    }

    public void testDerivationComparator() {

        NARComparator c = new NARComparator(new Default(), new NewDefault());
        c.input("<x --> y>.");
        c.input("<y --> z>.");

        System.out.println();

        int cycles = 16;
        for (int i = 0; i < cycles; i++) {
            if (!c.areEqual()) {

                System.out.println("\ncycle: " + c.time());
                System.out.println("original: " + c.getTaskSetA());
                System.out.println("rules: " + c.getTaskSetB());
//                System.out.println(c.getAMinusB());
//                System.out.println(c.getBMinusA());
            }
            c.frame(1);
        }



    }
}
