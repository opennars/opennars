package org.projog.core.function;

import org.projog.core.KB;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;

/**
 * Superclass of "plug-in" predicates that are not re-evaluated as part of backtracking.
 * <p>
 * Provides a skeletal implementation of {@link PredicateFactory} and {@link Predicate}. No attempt to find multiple
 * solutions will be made as part of backtracking as {@link #isRetryable()} always returns {@code false} - meaning
 * {@link Predicate#evaluate(PTerm...)} will never be invoked twice on a
 * {@code AbstractSingletonPredicate} for the same query. As they do not need to preserve state between calls to
 * {@link Predicate#evaluate(PTerm...)} {@code AbstractSingletonPredicate}s are state-less. As
 * {@code AbstractSingletonPredicate}s are state-less the same instance can be reused for the evaluation of all queries
 * of the predicate it represents. This is implemented by
 * {@link PredicateFactory#getPredicate(PTerm...)} always returning {@code this}.
 * <p>
 * 
 * @see AbstractRetryablePredicate
 * @see Predicate#evaluate(PTerm[])
 */
public abstract class AbstractSingletonPredicate extends AbstractPredicate implements PredicateFactory {
   private KB KB;

   /**
    * Returns {@code this}.
    */
   @Override
   public final Predicate getPredicate(PTerm... args) {
      return this;
   }

   @Override
   public final void setKB(KB KB) {
      this.KB = KB;
      init();
   }

   /**
    * This method is called by {@link #setKB(KB)}.
    * <p>
    * Can be overridden by subclasses to perform initialisation before any calls to {@link #evaluate(PTerm...)} are made.
    * As {@link #setKB(KB)} will have already been called before this method is invoked,
    * overridden versions will be able to access the {@code KnowledgeBase} using {@link #getKB()}.
    */
   protected void init() {
   }

   protected final KB getKB() {
      return KB;
   }

   @Override
   public final boolean isRetryable() {
      return false;
   }

   @Override
   public final boolean couldReEvaluationSucceed() {
      return false;
   }
}