package nars.term;

import nars.NAR;
import nars.nar.Default;
import nars.task.Task;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 3/19/15.
 */
public class TermNormalizationTest {

    public void test(String term, int[] v1Index, int[] v2Index) {
        //test for re-use of variable instances during normalization
        NAR n = new Default();
        Task t = n.inputTask(term + ".");
        Compound ct = t.getTerm();

        Term varA = ct.subterm(v1Index);
        assertEquals(Variable.class, varA.getClass());
        assertNotNull(varA);

        Term varB = ct.subterm(v2Index);
        assertEquals(Variable.class, varB.getClass());
        assertNotNull(varB);

        assertEquals(varA, varB);
        assertTrue("successfully re-used the variable instance", varA==varB);
    }

    @Test
    public void reuseVariableTermsDuringNormalization() {
        test("<<$1 --> x> ==> <$1 --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
        test("<<#1 --> x> ==> <#1 --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
        test("<<?x --> x> ==> <?x --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
    }
}
