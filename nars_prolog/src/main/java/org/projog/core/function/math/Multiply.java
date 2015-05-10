package org.projog.core.function.math;

/* TEST
 %LINK prolog-arithmetic
 */
/**
 * <code>*</code> - performs multiplication.
 */
public final class Multiply extends AbstractTwoArgumentsCalculatable {
   /** Returns the product of the two arguments */
   @Override
   protected double calculateDouble(double n1, double n2) {
      return n1 * n2;
   }

   /** Returns the product of the two arguments */
   @Override
   protected long calculateLong(long n1, long n2) {
      return n1 * n2;
   }
}