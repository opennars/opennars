package org.projog.core.function.math;

import org.projog.core.ProjogException;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;
import org.projog.core.term.PrologOperator;

import static org.projog.core.term.PrologOperator.INTEGER;

/**
 * A template for {@code Calculatable}s that accept two arguments of type {@link PrologOperator#INTEGER}.
 */
abstract class AbstractTwoIntegerArgumentsCalculatable extends AbstractCalculatable {
   @Override
   public final Numeric calculate(Numeric n1, Numeric n2) {
      final long i1 = toLong(n1);
      final long i2 = toLong(n2);
      return new IntegerNumber(calculateLong(i1, i2));
   }

   private long toLong(Numeric n) {
      if (n.type() == INTEGER) {
         return n.getLong();
      } else {
         throw new ProjogException("Expected integer but got: " + n.type() + " with value: " + n);
      }
   }

   /** Returns the result of evaluating an arithmetic expression using the two arguments */
   protected abstract long calculateLong(long n1, long n2);
}
