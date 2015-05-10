package org.projog.core.function.math;

/* TEST
 %LINK prolog-arithmetic
 */
/**
 * <code>-</code> - performs subtraction.
 */
public final class Subtract extends AbstractTwoArgumentsCalculatable {
   /** Returns the difference of the two arguments */
   @Override
   protected double calculateDouble(double n1, double n2) {
      return n1 - n2;
   }

   /** Returns the difference of the two arguments */
   @Override
   protected long calculateLong(long n1, long n2) {
      return n1 - n2;
   }
}