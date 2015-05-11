package org.projog.core.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TermTypeTest {
   @Test
   public void testIsNumeric() {
      assertTrue(PrologOperator.FRACTION.isNumeric());
      assertTrue(PrologOperator.INTEGER.isNumeric());

      assertFalse(PrologOperator.ATOM.isNumeric());
      assertFalse(PrologOperator.EMPTY_LIST.isNumeric());
      assertFalse(PrologOperator.LIST.isNumeric());
      assertFalse(PrologOperator.STRUCTURE.isNumeric());
      assertFalse(PrologOperator.NAMED_VARIABLE.isNumeric());
   }

   @Test
   public void testIsStructure() {
      assertTrue(PrologOperator.LIST.isStructure());
      assertTrue(PrologOperator.STRUCTURE.isStructure());

      assertFalse(PrologOperator.EMPTY_LIST.isStructure());
      assertFalse(PrologOperator.FRACTION.isStructure());
      assertFalse(PrologOperator.INTEGER.isStructure());
      assertFalse(PrologOperator.ATOM.isStructure());
      assertFalse(PrologOperator.NAMED_VARIABLE.isStructure());
   }

   @Test
   public void testIsVariable() {
      assertTrue(PrologOperator.NAMED_VARIABLE.isVariable());

      assertFalse(PrologOperator.FRACTION.isVariable());
      assertFalse(PrologOperator.INTEGER.isVariable());
      assertFalse(PrologOperator.ATOM.isVariable());
      assertFalse(PrologOperator.EMPTY_LIST.isVariable());
      assertFalse(PrologOperator.LIST.isVariable());
      assertFalse(PrologOperator.STRUCTURE.isVariable());
   }

   @Test
   public void testGetPrecedence() {
      assertEquals(1, PrologOperator.NAMED_VARIABLE.getPrecedence());
      assertEquals(2, PrologOperator.FRACTION.getPrecedence());
      assertEquals(3, PrologOperator.INTEGER.getPrecedence());
      assertEquals(4, PrologOperator.EMPTY_LIST.getPrecedence());
      assertEquals(5, PrologOperator.ATOM.getPrecedence());
      // all compound structures share the same precedence
      assertEquals(6, PrologOperator.STRUCTURE.getPrecedence());
      assertEquals(6, PrologOperator.LIST.getPrecedence());
   }
}