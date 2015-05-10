package org.projog.core.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.projog.core.term.EmptyList.EMPTY_LIST;

import org.junit.Test;

/**
 * @see TermTest
 */
public class EmptyListTest {
   @Test
   public void testGetName() {
      assertEquals(".", EMPTY_LIST.getName());
   }

   @Test
   public void testToString() {
      assertEquals("[]", EMPTY_LIST.toString());
   }

   @Test
   public void testGetTerm() {
      EmptyList e = EMPTY_LIST.get();
      assertSame(EMPTY_LIST, e);
   }

   @Test
   public void testGetType() {
      assertSame(TermType.EMPTY_LIST, EMPTY_LIST.type());
   }

   @Test
   public void testGetNumberOfArguments() {
      assertEquals(0, EMPTY_LIST.args());
   }

   @Test
   public void testGetArgument() {
      try {
         EMPTY_LIST.arg(0);
         fail();
      } catch (UnsupportedOperationException e) {
         // expected
      }
   }

   @Test
   public void testGetArgs() {
      try {
         EMPTY_LIST.getArgs();
         fail();
      } catch (UnsupportedOperationException e) {
         // expected
      }
   }
}