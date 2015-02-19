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

    NAR n = new NAR(new Default());
    PrologContext p = new PrologContext(n);

    @Test
    public void testFact() {

        n.addInput("fact(<x --> y>)!");
        n.run(5);
        assertTrue(p.getProlog(null).getTheory().toString().contains("inheritance(x,y)."));

    }

    @Test
    public void testFactual() {

        TextOutput.out(n);

        n.addInput("fact(<a --> y>)!");
        n.addInput("fact(<b --> y>)!");
        n.addInput("factual(<$q --> y>, #result)!");
        n.run(4);

        //contains("<$2 <-> {<x --> y>}>>")
        //..

    }
}
