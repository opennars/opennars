package nars.term;

import nars.NAR;
import nars.nar.Terminal;
import nars.task.Task;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 3/19/15.
 */
public class TermNormalizationTest {

    public void test(String term, int[] v1Index, int[] v2Index) {
        //test for re-use of variable instances during normalization
        NAR n = new Terminal();
        Task t = n.inputTask(term + ".");
        Compound ct = t.getTerm();

        Term varA = ct.subterm(v1Index);
        assertTrue(varA instanceof Variable);


        Term varB = ct.subterm(v2Index);
        assertTrue(varB instanceof Variable);

        assertEquals(varA, varB);
        assertTrue("successfully re-used the variable instance", varA==varB);
    }

    @Test
    public void reuseVariableTermsDuringNormalization() {
        test("<<$1 --> x> ==> <$1 --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
        //test("<<#1 --> x> ==> <#2 --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
        test("<<?x --> x> ==> <?x --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
    }
}
