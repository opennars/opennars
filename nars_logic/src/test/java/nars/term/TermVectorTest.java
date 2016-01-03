package nars.term;

import nars.$;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by me on 11/12/15.
 */
public class TermVectorTest {

    @Test
    public void testSubtermsEquality() {

        Compound a = (Compound) $.inh("a", "b");
        Compound b = (Compound) $.impl(Atom.the("a"), Atom.the("b"));

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

    @Test public void testSortedTermContainer() {
        TermVector a = new TermVector((Atom)$.$("a"), (Atom)$.$("b"));
        assertTrue(a.isSorted());
        TermVector b = new TermVector((Atom)$.$("b"), (Atom)$.$("a"));
        assertFalse(b.isSorted());
        TermVector s = TermSet.the(b.terms());
        assertTrue(s.isSorted());
        assertEquals(a, s);
        assertNotEquals(b, s);
    }

}
