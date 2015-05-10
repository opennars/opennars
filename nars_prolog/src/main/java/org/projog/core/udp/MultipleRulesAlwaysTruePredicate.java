package org.projog.core.udp;

import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.PTerm;

/**
 * Represents a user defined predicate that has a number of rules - each of which always successfully evaluate.
 * <p>
 * e.g.:
 * 
 * <pre>
 * p(_).
 * p(_).
 * p(_).
 * </pre>
 * </p>
 * <p>
 * Note: Similar to {@link org.projog.core.function.flow.RepeatSetAmount} - but implemented as a separate class rather
 * than reused as {@code RepeatSetAmount} only works with one argument but {@code MulitpleRulesAlwaysTruePredicate}
 * works with any number of arguments.
 */
public final class MultipleRulesAlwaysTruePredicate extends AbstractRetryablePredicate {
   private final int limit;
   private int ctr;

   public MultipleRulesAlwaysTruePredicate(int limit) {
      this.limit = limit;
   }

   @Override
   public boolean evaluate(PTerm... args) {
      return ctr++ < limit;
   }

   @Override
   public MultipleRulesAlwaysTruePredicate getPredicate(PTerm... args) {
      return new MultipleRulesAlwaysTruePredicate(limit);
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return ctr < limit;
   }
}
