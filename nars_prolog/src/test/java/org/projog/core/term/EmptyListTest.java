package org.projog.core.term;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.projog.core.term.EmptyList.EMPTY_LIST;

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
      assertSame(PrologOperator.EMPTY_LIST, EMPTY_LIST.type());
   }

   @Test
   public void testGetNumberOfArguments() {
      assertEquals(0, EMPTY_LIST.length());
   }

   @Test
   public void testGetArgument() {
      try {
         EMPTY_LIST.term(0);
         fail();
      } catch (UnsupportedOperationException e) {
         // expected
      }
   }

   @Test
   public void testGetArgs() {
      try {
         EMPTY_LIST.terms();
         fail();
      } catch (UnsupportedOperationException e) {
         // expected
      }
   }
}