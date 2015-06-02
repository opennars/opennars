package nars.core;

import nars.NAR;
import nars.model.impl.Default;
import nars.nal.term.Compound;
import nars.nal.Task;
import nars.nal.term.Variable;
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
        NAR n = new NAR(new Default());
        Task t = n.inputTask(term + ".");
        Compound ct = t.sentence.term;
        Variable a = ct.subterm(v1Index);
        assertNotNull(a);
        Variable b = ct.subterm(v2Index);
        assertNotNull(b);
        assertEquals(a, b);
        assertTrue("successfully re-used the variable instance", a==b);
    }

    @Test
    public void reuseVariableTermsDuringNormalization() {
        test("<<$1 --> x> ==> <$1 --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
        test("<<#1 --> x> ==> <#1 --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
        test("<<?x --> x> ==> <?x --> y>>", new int[] { 0, 0 }, new int[] { 1, 0 });
    }
}
