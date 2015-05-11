package org.projog.core;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/**
 * Represents all predicates that a {@code KnowledgeBase} has no definition of.
 * <p>
 * Always fails to evaluate successfully.
 * 
 * @see KB#getPredicateFactory(PredicateKey)
 * @see KB#getPredicateFactory(PTerm)
 */
public final class UnknownPredicate extends AbstractSingletonPredicate {
   /**
    * Singleton instance
    */
   public static final UnknownPredicate UNKNOWN_PREDICATE = new UnknownPredicate();

   /**
    * Private constructor to force use of {@link #UNKNOWN_PREDICATE}
    */
   private UnknownPredicate() {
      // do nothing
   }

   @Override
   public boolean evaluate(PTerm... args) {
      return false;
   }
}