package nars.term;

import org.junit.Test;

import static nars.$.$;
import static org.junit.Assert.*;

/**
 * Created by me on 3/19/15.
 */
public class TermNormalizationTest {

    @Test
    public void reuseVariableTermsDuringNormalization2() {
        for (String v : new String[] { "?a", "?b", "#a", "#c" }) {
            Compound eq = $("<<" + v +" --> b> </> <" + v + " --> c>>");
            Term a = eq.subterm(0, 0);
            Term b = eq.subterm(1, 0);
            assertNotEquals(a, eq.subterm(0, 1));
            assertEquals(a, b);
            assertTrue(a == b);
        }
    }

}
