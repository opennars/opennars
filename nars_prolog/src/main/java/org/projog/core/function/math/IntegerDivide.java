package org.projog.core.function.math;


/* TEST
 %LINK prolog-arithmetic
 */
/**
 * <code>//</code> - performs integer division.
 * <p>
 * The result will be rounded towards zero. e.g. <code>7 // 2</code> is rounded down to <code>3</code> while
 * <code>-7 // 2</code> is rounded up to <code>-3</code>
 */
public final class IntegerDivide extends AbstractTwoIntegerArgumentsCalculatable {
   @Override
   protected long calculateLong(long dividend, long divisor) {
      return dividend / divisor;
   }
}