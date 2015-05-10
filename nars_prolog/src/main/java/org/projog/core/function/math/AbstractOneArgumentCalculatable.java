package org.projog.core.function.math;

import org.projog.core.term.DecimalFraction;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;
import org.projog.core.term.TermType;

/**
 * A template for {@code Calculatable}s that accept exactly one argument.
 */
abstract class AbstractOneArgumentCalculatable extends AbstractCalculatable {
   @Override
   public final Numeric calculate(Numeric n) {
      if (n.type() == TermType.FRACTION) {
         double answer = calculateDouble(n.getDouble());
         return new DecimalFraction(answer);
      } else {
         long answer = calculateLong(n.getLong());
         return new IntegerNumber(answer);
      }
   }

   /** Returns the result of evaluating an arithmetic expression using the specified argument */
   protected abstract double calculateDouble(double n);

   /** Returns the result of evaluating an arithmetic expression using the specified argument */
   protected abstract long calculateLong(long n);
}
