package org.projog.core.udp;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/**
 * Represents a user defined predicate that always successfully evaluates exactly once.
 * <p>
 * e.g.: {@code p(_).}
 * </p>
 * <p>
 * Note: Similar to {@link org.projog.core.function.bool.True} - but implemented as a separate class rather than reused
 * as {@code True} only works with no arguments but {@code SingleRuleAlwaysTruePredicate} works with any number of
 * arguments.
 */
public final class SingleRuleAlwaysTruePredicate extends AbstractSingletonPredicate {
   public final static SingleRuleAlwaysTruePredicate SINGLETON = new SingleRuleAlwaysTruePredicate();

   /** @see #SINGLETON */
   private SingleRuleAlwaysTruePredicate() {
   }

   @Override
   public boolean evaluate(PTerm... args) {
      return true;
   }
}
