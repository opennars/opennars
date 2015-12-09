package nars.term;

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

        assertTrue( inh("a", "b").hasAny(Op.ATOM) );
        assertTrue( inh( p("a"), $("b") ).hasAny(Op.or(Op.ATOM, Op.PRODUCT) ) );
        assertFalse( inh("a", "b").hasAny(Op.INHERIT) );
        assertTrue( inh("a", "b").isAny(Op.INHERIT) );
    }
}
