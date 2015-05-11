package nars.prolog;

import nars.NAR;
import nars.model.impl.Default;
import org.junit.Test;

/**
 * Created by me on 5/10/15.
 */
public class NARPrologAgentTest {

    @Test
    public void init1() throws InterruptedException {
        NAR n = new NAR(new Default());
        new NARPrologAgent(n);
    }
}
