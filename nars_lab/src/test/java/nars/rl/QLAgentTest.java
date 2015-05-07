package nars.rl;

import jurls.reinforcementlearning.domains.RLEnvironment;
import jurls.reinforcementlearning.domains.follow.Follow1D;
import nars.Global;
import nars.NAR;
import nars.model.impl.Default;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by me on 5/7/15.
 */
public class QLAgentTest {



    @Test
    public void testQLAgent() {

        Global.DEBUG = true;

        RLEnvironment env = new Follow1D();

        int concepts = 16;
        NAR n = new NAR( new Default(concepts, 1, 1) );

        QLAgent a = new QLAgent(n, "act", "<good --> be>", env,
                new RawPerception("s", 0.5f));

        n.frame();
        n.frame();

        System.out.println("Columns: " + a.columns());

        //n.memory.concepts.forEach(x -> System.out.println(x));
        assertEquals(concepts, n.memory.concepts.size());
    }
}
