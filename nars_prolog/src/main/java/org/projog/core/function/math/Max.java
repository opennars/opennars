package org.projog.core.function.math;

import org.projog.core.term.Numeric;

/* TEST
 %QUERY X is max(5,5)
 %ANSWER X=5
 %QUERY X is max(7,8)
 %ANSWER X=8
 %QUERY X is max(3,2)
 %ANSWER X=3
 %QUERY X is max(2.5,2.5)
 %ANSWER X=2.5
 %QUERY X is max(2.75,2.5)
 %ANSWER X=2.75
 %QUERY X is max(1,1.5)
 %ANSWER X=1.5
 %QUERY X is max(2,1.5)
 %ANSWER X=2
 %QUERY X is max(-3,2)
 %ANSWER X=2
 %QUERY X is max(-3,-2)
 %ANSWER X=-2
 %QUERY X is max(-2.5,-2.25)
 %ANSWER X=-2.25
 %QUERY X is max(0,0)
 %ANSWER X=0
 %QUERY X is max(0.0,0.0)
 %ANSWER X=0.0
 %QUERY X is max(0,0.0)
 %ANSWER X=0.0
 %QUERY X is max(0.0,0)
 %ANSWER X=0
 */
/**
 * <code>max</code> - finds the maximum of two numbers.
 */
public final class Max extends AbstractCalculatable {
   @Override
   protected Numeric calculate(Numeric n1, Numeric n2) {
      if (n1.getDouble() > n2.getDouble()) {
         return n1;
      } else {
         return n2;
      }
   }
}
