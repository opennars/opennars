package org.projog.core.term;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.projog.TestUtils.decimalFraction;

/**
 * @see TermTest
 */
public class DecimalFractionTest {
   private static final double DELTA = 0;

   @Test
   public void testGetName() {
      assertEquals("0.0", new DecimalFraction(0).getName());
      assertEquals(Double.toString(Double.MAX_VALUE), new DecimalFraction(Double.MAX_VALUE).getName());
      assertEquals("-7.0", new DecimalFraction(-7).getName());
   }

   @Test
   public void testToString() {
      assertEquals("0.0", new DecimalFraction(0).toString());
      assertEquals(Double.toString(Double.MAX_VALUE), new DecimalFraction(Double.MAX_VALUE).toString());
      assertEquals("-7.0", new DecimalFraction(-7).toString());
   }

   @Test
   public void testGetTerm() {
      DecimalFraction d1 = new DecimalFraction(0);
      DecimalFraction d2 = d1.get();
      assertSame(d1, d2);
   }

   @Test
   public void testGetLong() {
      assertEquals(0, new DecimalFraction(0).getLong());
      assertEquals(Long.MAX_VALUE, new DecimalFraction(Long.MAX_VALUE).getLong());
      assertEquals(-7, new DecimalFraction(-7.01).getLong());
      assertEquals(-1, new DecimalFraction(-1.01).getLong());
   }

   @Test
   public void testGetDouble() {
      assertEquals(0.0, new DecimalFraction(0).getDouble(), DELTA);
      assertEquals(Double.MAX_VALUE, new DecimalFraction(Double.MAX_VALUE).getDouble(), DELTA);
      assertEquals(-7.01, new DecimalFraction(-7.01).getDouble(), DELTA);
   }

   @Test
   public void testGetType() {
      DecimalFraction d = decimalFraction();
      assertSame(PrologOperator.FRACTION, d.type());
   }

   @Test
   public void testGetNumberOfArguments() {
      DecimalFraction d = decimalFraction();
      assertEquals(0, d.length());
   }

   @Test
   public void testGetArgument() {
      try {
         DecimalFraction d = decimalFraction();
         d.term(0);
         fail();
      } catch (UnsupportedOperationException e) {
         // expected
      }
   }

   @Test
   public void testGetArgs() {
      DecimalFraction d = decimalFraction();
      assertSame(TermUtils.EMPTY_ARRAY, d.terms());
   }
}