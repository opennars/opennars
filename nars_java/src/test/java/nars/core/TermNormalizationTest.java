package nars.core;

import nars.build.Default;
import nars.logic.entity.CompoundTerm;
import nars.logic.entity.Task;
import nars.logic.entity.Variable;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 3/19/15.
 */
public class TermNormalizationTest {

    @Test
    public void reuseVariableTermsDuringNormalization() {
        //test for re-use of variable instances during normalization
        NAR n = new NAR(new Default());
        Task t = n.inputTask("<<$1 --> x> ==> <$1 --> y>>.");
        CompoundTerm ct = t.sentence.term;
        //Variable a = ((CompoundTerm)ct.term[0]).term[0];
        Variable a = ct.subterm(0,0);
        assertNotNull(a);
        Variable b = ct.subterm(1,0);
        assertNotNull(b);
        assertEquals(a, (b));
        assertTrue("successfully re-used the variable instance", a==b);

    }
}
