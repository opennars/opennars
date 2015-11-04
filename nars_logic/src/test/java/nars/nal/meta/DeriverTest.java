package nars.nal.meta;

import nars.NAR;
import nars.nar.Default;
import nars.term.Term;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 9/15/15.
 */
public class DeriverTest {

    @Test
    public void testImpossibility() {
        //impossible? 1000001 (<b --> c>, <a --> b>)1000010 (<%1 --> %2>, <%1 --> %3>)
        NAR n = new Default();
        Term a = n.term("(<b --> c>, <a --> b>)");
        Term b = n.term("(<%1 --> %2>, <%1 --> %3>)");
        Term c = n.term("(<b --> c>, #d)");
        assertTrue(a.impossibleToMatch(c));
        assertTrue(b.impossibleToMatch(c));
        assertTrue(!a.impossibleToMatch(b));
        assertTrue(b.impossibleToMatch(a));


    }
}
