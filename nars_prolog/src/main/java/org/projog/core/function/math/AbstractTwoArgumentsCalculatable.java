package org.projog.core.function.math;

import org.projog.core.term.DecimalFraction;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;
import org.projog.core.term.TermType;

/**
 * A template for {@code Calculatable}s that accept two arguments.
 */
abstract class AbstractTwoArgumentsCalculatable extends AbstractCalculatable {
   @Override
   public final Numeric calculate(Numeric n1, Numeric n2) {
      if (containsFraction(n1, n2)) {
         double answer = calculateDouble(n1.getDouble(), n2.getDouble());
         return new DecimalFraction(answer);
      } else {
         long answer = calculateLong(n1.getLong(), n2.getLong());
         return new IntegerNumber(answer);
      }
   }

   private static boolean containsFraction(Numeric n1, Numeric n2) {
      return n1.type() == TermType.FRACTION || n2.type() == TermType.FRACTION;
   }

   /** Returns the result of evaluating an arithmetic expression using the two arguments */
   protected abstract double calculateDouble(double n1, double n2);

   /** Returns the result of evaluating an arithmetic expression using the two arguments */
   protected abstract long calculateLong(long n1, long n2);
}