package nars.term;

import junit.framework.TestCase;
import nars.Op;
import org.junit.Test;

import static nars.$.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * test term hash and structure bits
 */
public class TermHashTest {

    @Test
    public void testStructureIsVsHas() {

        assertTrue(inh("a", "b").hasAny(Op.ATOM));
        assertTrue(inh(p("a"), $("b"))
                .hasAny(Op.or(Op.ATOM, Op.PRODUCT)));

        assertFalse(inh(p("a"), $("b"))
                .isAny(Op.or(Op.SIMILAR, Op.PRODUCT)));
        assertFalse(inh(p("a"), $("b"))
                .isAny(Op.PRODUCT));

        assertTrue(inh("a", "b").hasAny(Op.INHERIT));
        assertTrue(inh("a", "b").isAny(Op.INHERIT));
    }

    @Test
    public void testTemporalBits() {
        Term x = $("<(&&,%1,%2)=\\>%3>");
        assertTrue(x
                .isAny(Op.TemporalBits));

        TestCase.assertFalse(x
                .isAny(Op.CONJUNCTION.bit()));
        assertTrue(x
                .hasAny(Op.CONJUNCTION.bit()));

    }

}
