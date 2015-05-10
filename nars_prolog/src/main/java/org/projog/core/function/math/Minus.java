package org.projog.core.function.math;

/* TEST
 %LINK prolog-arithmetic
 */
/**
 * <code>-</code> - minus operator.
 */
public final class Minus extends AbstractOneArgumentCalculatable {
   @Override
   protected double calculateDouble(double n) {
      return -n;
   }

   @Override
   protected long calculateLong(long n) {
      return -n;
   }
}
