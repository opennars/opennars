package org.projog.core.term;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.Calculatables;
import org.projog.core.KB;
import org.projog.core.ProjogException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.*;
import static org.projog.core.KnowledgeBaseUtils.getCalculatables;
import static org.projog.core.term.NumericTermComparator.NUMERIC_TERM_COMPARATOR;

public class NumericTermComparatorTest {
   private final KB kb = TestUtils.createKnowledgeBase();
   private final Calculatables calculatables = getCalculatables(kb);

   @Test
   public void testCompareDecimalValues() {
      compare(decimalFraction(2.1), integerNumber(2));
      compare(decimalFraction(2.1), decimalFraction(2.1));
      compare(decimalFraction(2.1), decimalFraction(2.11));
      compare(decimalFraction(2.1), decimalFraction(-2.1));
   }

   @Test
   public void testCompareIntegerValues() {
      int[] values = {0, 1, 2, 7, -1, -2, 7, Integer.MIN_VALUE, Integer.MAX_VALUE};
      for (int i1 = 0; i1 < values.length; i1++) {
         for (int i2 = i1; i2 < values.length; i2++) {
            compare(values[i1], values[i2]);
         }
      }
   }

   @Test
   public void testAtoms() {
      try {
         NUMERIC_TERM_COMPARATOR.compare(atom("a"), atom("b"));
         fail();
      } catch (ProjogException e) {
         assertEquals("Expected Numeric but got: ATOM with value: a", e.getMessage());
      }
   }

   @Test
   public void testUnassignedVariables() {
      try {
         NUMERIC_TERM_COMPARATOR.compare(variable("X"), variable("Y"));
         fail();
      } catch (ProjogException e) {
         assertEquals("Expected Numeric but got: NAMED_VARIABLE with value: X", e.getMessage());
      }
   }

   @Test
   public void testAssignedVariables() {
      PTerm x = variable("X");
      PTerm y = variable("Y");
      x.unify(integerNumber(2));
      y.unify(integerNumber(2));
      assertEquals(0, NUMERIC_TERM_COMPARATOR.compare(x, y));
   }

   /**
    * NumericTermComparator provides an overloaded version of {@link NumericTermComparator#compare(PTerm, PTerm)} that
    * also accepts a {@code KnowledgeBase} argument - this method tests that overloaded version.
    * 
    * @see NumericTermComparator#compare(PTerm, PTerm, KB)
    * @see #testStructuresRepresentingCalculatables
    */
   @Test
   public void testOverloadedCompareMethod() {
      compare("1+1", "5-3", kb, 0);
      compare("1.5", "3/2.0", kb, 0);
      compare("7*5", "4*9", kb, -1); //35v36
      compare("72", "8*9", kb, 0);
      compare("72", "60+13", kb, -1);
      compare("72", "74-3", kb, 1);
   }

   /**
    * Test that {@link NumericTermComparator#compare(PTerm, PTerm, KB)} tries to evaluate {@code Structure}s
    * that represent arithmetic expressions but {@link NumericTermComparator#compare(PTerm, PTerm)} throws a
    * {@code ProjogException}
    */
   @Test
   public void testStructuresRepresentingCalculatables() {
      PStruct addition = structure("+", integerNumber(1), integerNumber(3));
      PStruct subtraction = structure("-", integerNumber(5), integerNumber(2));

      // test compare(Term, Term) throws a ProjogException for when
      // a parameter is a structure (even if it represents an arithmetic expression).
      try {
         NUMERIC_TERM_COMPARATOR.compare(addition, subtraction);
         fail();
      } catch (ProjogException e) {
         assertEquals("Expected Numeric but got: STRUCTURE with value: +(1, 3)", e.getMessage());
      }
      try {
         NUMERIC_TERM_COMPARATOR.compare(integerNumber(1), subtraction);
         fail();
      } catch (ProjogException e) {
         assertEquals("Expected Numeric but got: STRUCTURE with value: -(5, 2)", e.getMessage());
      }

      // test compare(Term, Term) evaluates structures representing arithmetic expressions
      assertEquals(1, NUMERIC_TERM_COMPARATOR.compare(addition, subtraction, calculatables));
      assertEquals(-1, NUMERIC_TERM_COMPARATOR.compare(subtraction, addition, calculatables));
      assertEquals(0, NUMERIC_TERM_COMPARATOR.compare(addition, addition, calculatables));

      // test compare(Term, Term, KnowledgeBase) throws a ProjogException if
      // a structure can not be evaluated as an arithmetic expression
      try {
         NUMERIC_TERM_COMPARATOR.compare(addition, structure("-", integerNumber(5), atom()), calculatables);
         fail();
      } catch (ProjogException e) {
         assertEquals("Cannot find calculatable: test", e.getMessage());
      }
      try {
         NUMERIC_TERM_COMPARATOR.compare(structure("~", integerNumber(5), integerNumber(2)), subtraction, calculatables);
         fail();
      } catch (ProjogException e) {
         assertEquals("Cannot find calculatable: ~/2", e.getMessage());
      }
   }

   private void compare(int i1, int i2) {
      compare(integerNumber(i1), integerNumber(i2));
      compare(decimalFraction(i1), decimalFraction(i2));
      compare(decimalFraction(i1), integerNumber(i2));
   }

   private void compare(IntegerNumber t1, IntegerNumber t2) {
      Long i1 = t1.getLong();
      Long i2 = t2.getLong();
      assertEquals(i1.compareTo(i2), NUMERIC_TERM_COMPARATOR.compare(t1, t2));
      assertEquals(i2.compareTo(i1), NUMERIC_TERM_COMPARATOR.compare(t2, t1));
   }

   private void compare(DecimalFraction t1, DecimalFraction t2) {
      Double d1 = t1.getDouble();
      Double d2 = t2.getDouble();
      assertEquals(d1.compareTo(d2), NUMERIC_TERM_COMPARATOR.compare(t1, t2));
      assertEquals(d2.compareTo(d1), NUMERIC_TERM_COMPARATOR.compare(t2, t1));
   }

   private void compare(DecimalFraction t1, IntegerNumber t2) {
      Double d1 = t1.getDouble();
      Double d2 = t2.getDouble();
      assertEquals(d1 + " " + d2, d1.compareTo(d2), NUMERIC_TERM_COMPARATOR.compare(t1, t2));
      assertEquals(d2 + " " + d1, d2.compareTo(d1), NUMERIC_TERM_COMPARATOR.compare(t2, t1));
   }

   private void compare(String s1, String s2, KB kb, int expected) {
      PTerm t1 = TestUtils.parseSentence(s1 + ".");
      PTerm t2 = TestUtils.parseSentence(s2 + ".");
      assertEquals(expected, NUMERIC_TERM_COMPARATOR.compare(t1, t2, calculatables));
      assertEquals(0 - expected, NUMERIC_TERM_COMPARATOR.compare(t2, t1, calculatables));
   }
}