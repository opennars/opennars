package nars.rl;

import jurls.reinforcementlearning.domains.RLEnvironment;
import jurls.reinforcementlearning.domains.follow.Follow1D;
import nars.Global;
import nars.NAR;
import nars.io.TextOutput;
import nars.model.impl.Default;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by me on 5/7/15.
 */
public class QLAgentTest {



    @Test
    public void testQLAgents() {
        testQLAgent(4);
        testQLAgent(16);
        testMatrixState(testQLAgent(128));
    }

    private NAR testMatrixState(NAR n) {
        //n.memory.concepts.forEach(x -> System.out.println(x));


        return n;
    }

    public NAR testQLAgent(int conceptCapacity) {

        Global.DEBUG = true;

        RLEnvironment env = new Follow1D();

        NAR n = new NAR( new Default(conceptCapacity, 1, 1) );

        QLAgent a = new QLAgent(n, "act", "<good --> be>", env,
                new RawPerception.BipolarDirectPerception("s", 0.5f));

        //TODO fluent api to define perceptual hierarchy:
        //new QLAgent(n).in(env).in(new RawPerception, new SOM, ..)

        //allow the concept memory to reach capacity
        for (int i = 0; i < conceptCapacity / 4; i++)
            n.frame();


        if (conceptCapacity < 20) {
            //TODO ideally we want the capacity and max size to be equal, but for now it's 1 less than capacity
            assertTrue(n.memory.concepts.size() + " concepts for capacity=" + conceptCapacity, Math.abs(conceptCapacity - n.memory.concepts.size()) <= 1);
        }
        else {

            //TextOutput.out(n);

            for (int i = 0; i < 8; i++)
                n.frame();


            //a.getOperatorConcept().termLinks.printAll(System.out);
            //a.getActionConcept(0).termLinks.printAll(System.out);
            a.getActionConcept(0).print(System.out);

            //check that the agent knows all the actions
            assertEquals(a.ql.cols.toString(), a.ql.cols.size(), env.numActions());


            assertTrue(a.ql.rows.toString(), a.ql.rows.size() > 0);
        }

        return n;
    }
}
