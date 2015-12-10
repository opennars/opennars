package nars.term;

import nars.$;
import nars.nal.nal5.Implication;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by me on 11/12/15.
 */
public class TermVectorTest {

    @Test
    public void testSubtermsEquality() {

        Compound a = $.inh("a", "b");
        Compound b = (Compound) Implication.implication(Atom.the("a"), Atom.the("b"));

        assertEquals(a.subterms(), b.subterms());
        assertEquals(a.subterms().hashCode(), b.subterms().hashCode());

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());

        assertEquals(0, a.subterms().compareTo(b.subterms()));
        assertEquals(0, b.subterms().compareTo(a.subterms()));

        assertNotEquals(0, a.compareTo(b));
        assertNotEquals(0, b.compareTo(a));

        /*assertTrue("after equality test, subterms vector determined shareable",
                a.subterms() == b.subterms());*/


    }


}
