package org.projog.core.function.math;

/* TEST
 %LINK prolog-arithmetic
 */
/**
 * <code>rem</code> - finds the remainder of division of one number by another.
 * <p>
 * The result has the same sign as the dividend (i.e. first argument).
 */
public final class Remainder extends AbstractTwoIntegerArgumentsCalculatable {
   @Override
   protected long calculateLong(long numerator, long divider) {
      return numerator % divider;
   }
}