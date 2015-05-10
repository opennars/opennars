package org.projog.core.function.compare;

import org.projog.core.term.PTerm;

/* TEST
 %TRUE 2>1
 %FALSE 2>2
 %FALSE 2>3
 %TRUE 3-1>1
 %FALSE 1+1>4-2
 %FALSE 8/4>9/3
 %FALSE 1.5>3.0/2.0
 */
/**
 * <code>X&gt;Y</code> - numeric "greater than" test.
 * <p>
 * Succeeds when the number argument <code>X</code> is greater than the number argument <code>Y</code>.
 * </p>
 */
public final class NumericGreaterThan extends AbstractNumericComparisonPredicate {
   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2) {
      return compare(arg1, arg2) == 1;
   }
}