package org.projog.core.function.compare;

import org.projog.core.term.PTerm;

/* TEST
 %TRUE 1=:=1
 %TRUE 1.5=:=3.0/2.0
 %TRUE 6*6=:=9*4
 %FALSE 1=:=2
 %FALSE 1+1=:=1-1
 %QUERY X=1, Y=1, X=:=Y
 %ANSWER
 % X=1
 % Y=1
 %ANSWER
 %FALSE X=1, Y=2, X=:=Y
 */
/**
 * <code>X=:=Y</code> - numeric equality test.
 * <p>
 * Succeeds when the number argument <code>X</code> is equal to the number argument <code>Y</code>.
 * </p>
 */
public final class NumericEquality extends AbstractNumericComparisonPredicate {
   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2) {
      return compare(arg1, arg2) == 0;
   }
}