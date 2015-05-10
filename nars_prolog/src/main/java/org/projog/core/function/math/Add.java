package org.projog.core.function.math;

/* TEST
 %LINK prolog-arithmetic
 */
/**
 * <code>+</code> - performs addition.
 */
public final class Add extends AbstractTwoArgumentsCalculatable {
   /** Returns the sum of the two arguments */
   @Override
   protected double calculateDouble(double n1, double n2) {
      return n1 + n2;
   }

   /** Returns the sum of the two arguments */
   @Override
   protected long calculateLong(long n1, long n2) {
      return n1 + n2;
   }
}