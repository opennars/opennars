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
      Atom a = new Atom("test");
      assertEquals("test", a.getName());
   }

   @Test
   public void testToString() {
      Atom a = new Atom("test");
      assertEquals("test", a.toString());
   }

   @Test
   public void testGetTerm() {
      Atom a = atom();
      Atom b = a.get();
      assertSame(a, b);
   }

   @Test
   public void testGetType() {
      Atom a = atom();
      assertSame(TermType.ATOM, a.type());
   }

   @Test
   public void testGetNumberOfArguments() {
      Atom a = atom();
      assertEquals(0, a.args());
   }

   @Test
   public void testGetArgument() {
      try {
         Atom a = atom();
         a.arg(0);
         fail();
      } catch (UnsupportedOperationException e) {
         // expected
      }
   }

   @Test
   public void testGetArgs() {
      Atom a = atom();
      assertSame(TermUtils.EMPTY_ARRAY, a.getArgs());
   }
}