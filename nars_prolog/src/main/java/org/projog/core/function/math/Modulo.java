package org.projog.core.function.math;


/* TEST
 %LINK prolog-arithmetic
 */
/**
 * <code>mod</code> - finds the remainder of division of one number by another.
 * <p>
 * The result has the same sign as the divisor (i.e. second argument).
 */
public final class Modulo extends AbstractTwoIntegerArgumentsCalculatable {
   @Override
   protected long calculateLong(long numerator, long divider) {
      final long modulo = numerator % divider;
      if (modulo == 0 || numerator * divider > 0) {
         return modulo;
      } else {
         return modulo + divider;
      }
   }
}