package org.projog.core.function.compare;

import org.projog.core.term.PTerm;

/* TEST
 %FALSE 2<1
 %FALSE 2<2
 %TRUE 2<3
 %FALSE 3-1<1
 %FALSE 1+1<4-2
 %TRUE 8/4<9/3
 %FALSE 1.5<3.0/2.0
 */
/**
 * <code>X&lt;Y</code> - numeric "less than" test.
 * <p>
 * Succeeds when the number argument <code>X</code> is less than the number argument <code>Y</code>.
 * </p>
 */
public final class NumericLessThan extends AbstractNumericComparisonPredicate {
   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2) {
      return compare(arg1, arg2) == -1;
   }
}