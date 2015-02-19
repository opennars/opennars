package nars;

import nars.build.Default;
import nars.core.NAR;
import nars.io.TextOutput;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 2/19/15.
 */
public class NARPrologTest {

    @Test
    public void testFact() {
        NAR n = new NAR(new Default());

        TextOutput.out(n);

        PrologContext p = new PrologContext(n);

        n.addInput("fact(<x --> y>)!");
        n.run(5);

        assertTrue(p.getProlog(null).getTheory().toString().contains("inheritance(x,y)."));

    }
}
