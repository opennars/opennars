package org.projog.core.function.compare;

import org.projog.core.term.PTerm;

/* TEST
 %FALSE 1=\=1
 %FALSE 1.5=\=3.0/2.0
 %FALSE 6*6=\=9*4
 %TRUE 1=\=2
 %TRUE 1+1=\=1-1
 %FALSE X=1, Y=1, X=\=Y
 %QUERY X=1, Y=2, X=\=Y
 %ANSWER
 % X=1
 % Y=2
 %ANSWER
 */
/**
 * <code>X=\=Y</code> - numeric inequality test.
 * <p>
 * Succeeds when the number argument <code>X</code> is <i>not</i> equal to the number argument <code>Y</code>.
 * </p>
 */
public final class NumericInequality extends AbstractNumericComparisonPredicate {
   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2) {
      return compare(arg1, arg2) != 0;
   }
}