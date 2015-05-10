package org.projog.core.function.math;

import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;

/* TEST
 validate_in_range(X) :- Y is random(X), Y>=0, Y<X.
 
 %TRUE validate_in_range(3), validate_in_range(7), validate_in_range(100)

 %QUERY X is random(1)
 %ANSWER X=0
 */
/**
 * <code>random(X)</code> Evaluate to a random integer i for which 0 =< i < X.
 */
public final class Random extends AbstractCalculatable {
   @Override
   public Numeric calculate(Numeric n) {
      long max = n.getLong();
      return new IntegerNumber((long) (Math.random() * max));
   }
}
