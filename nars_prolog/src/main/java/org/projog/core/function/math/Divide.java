package org.projog.core.function.math;

import org.projog.core.term.DecimalFraction;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;
import org.projog.core.term.PrologOperator;

/* TEST
 %LINK prolog-arithmetic
 */
/**
 * <code>/</code> - performs division.
 */
public final class Divide extends AbstractCalculatable {
   @Override
   public Numeric calculate(Numeric n1, Numeric n2) {
      if (containsFraction(n1, n2)) {
         return divideFractions(n1, n2);
      } else {
         long dividend = n1.getLong();
         long divisor = n2.getLong();
         if (dividend % divisor == 0) {
            // e.g. 6 / 2 = 3
            return new IntegerNumber(dividend / divisor);
         } else {
            // e.g. 7 / 2 = 3.5
            return divideFractions(n1, n2);
         }
      }
   }

   private static boolean containsFraction(Numeric n1, Numeric n2) {
      return n1.type() == PrologOperator.FRACTION || n2.type() == PrologOperator.FRACTION;
   }

   private DecimalFraction divideFractions(Numeric n1, Numeric n2) {
      return new DecimalFraction(n1.getDouble() / n2.getDouble());
   }
}