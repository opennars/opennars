package org.projog.core.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.atom;

import org.junit.Test;

/**
 * @see TermTest
 */
public class AtomTest {
   @Test
   public void testGetName() {
      PAtom a = new PAtom("test");
      assertEquals("test", a.getName());
   }

   @Test
   public void testToString() {
      PAtom a = new PAtom("test");
      assertEquals("test", a.toString());
   }

   @Test
   public void testGetTerm() {
      PAtom a = atom();
      PAtom b = a.get();
      assertSame(a, b);
   }

   @Test
   public void testGetType() {
      PAtom a = atom();
      assertSame(PrologOperator.ATOM, a.type());
   }

   @Test
   public void testGetNumberOfArguments() {
      PAtom a = atom();
      assertEquals(0, a.length());
   }

   @Test
   public void testGetArgument() {
      try {
         PAtom a = atom();
         a.term(0);
         fail();
      } catch (UnsupportedOperationException e) {
         // expected
      }
   }

   @Test
   public void testGetArgs() {
      PAtom a = atom();
      assertSame(TermUtils.EMPTY_ARRAY, a.terms());
   }
}