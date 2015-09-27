package nars.nal.nal6;

import nars.NAR;
import nars.Op;
import nars.meter.TestNAR;
import nars.nar.Default;
import nars.util.graph.TermLinkGraph;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 9/26/15.
 */
public class IndependentVariableIntroductionTest {

    final String somethingIsBird = "bird:$x";
    final String somethingIsAnimal = "animal:$x";

    @Test public void testA() {
        testIntroduction(somethingIsBird, Op.IMPLICATION, somethingIsAnimal, "bird:robin", "animal:robin");
    }
    @Test public void testB1() {
        testIntroduction(somethingIsBird, Op.EQUIVALENCE, somethingIsAnimal, "animal:robin", "bird:robin");
    }
    @Test public void testB2() {
        testIntroduction(somethingIsBird, Op.EQUIVALENCE, somethingIsAnimal, "bird:robin", "animal:robin");
    }




    public void testIntroduction(String subj, Op relation, String pred, String belief, String concl) {

        new TestNAR(new Default().nal(6))
                .believe("<" + subj + " " + relation + " " + pred + ">")
                .believe(belief)
                .mustBelieve(2, concl, 0.81f)
                .run();
                //.next()
                //.run(1).assertTermLinkGraphConnectivity();

    }

    @Test
    public void testIndVarConnectivity() {

        String c = "<<$x --> bird> ==> <$x --> animal>>.";

        NAR n = new Default().nal(6);
        n.input(c);
        n.frame(1);

        TermLinkGraph g = new TermLinkGraph(n);
        assertTrue("termlinks form a fully connected graph:\n" + g.toString(), g.isConnected());

    }


}
