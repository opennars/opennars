package org.projog.core.function;

import org.projog.core.KnowledgeBase;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;

/**
 * Superclass of "plug-in" predicates that are re-evaluated as part of backtracking.
 * <p>
 * Provides a skeletal implementation of {@link PredicateFactory} and {@link Predicate}. As {@link #isRetryable()}
 * always returns {@code true} {@link Predicate#evaluate(PTerm...)} may be invoked on a
 * {@code code AbstractRetryablePredicate} multiple times for the same query. If a {@code AbstractRetryablePredicate}
 * need to preserve state between calls to {@link Predicate#evaluate(PTerm...)} then it's
 * implementation of {@link PredicateFactory#getPredicate(PTerm...)} should return a new instance
 * each time.
 * 
 * @see AbstractSingletonPredicate
 * @see org.projog.core.Predicate#evaluate(PTerm[])
 * @see org.projog.core.PredicateFactory#getPredicate(PTerm[])
 */
public abstract class AbstractRetryablePredicate extends AbstractPredicate implements PredicateFactory {
   private KnowledgeBase knowledgeBase;

   @Override
   public Predicate getPredicate(PTerm... args) {
      switch (args.length) {
         case 0:
            return getPredicate();
         case 1:
            return getPredicate(args[0]);
         case 2:
            return getPredicate(args[0], args[1]);
         case 3:
            return getPredicate(args[0], args[1], args[2]);
         default:
            throw createWrongNumberOfArgumentsException(args.length);
      }
   }

   protected Predicate getPredicate() {
      throw createWrongNumberOfArgumentsException(0);
   }

   protected Predicate getPredicate(PTerm arg) {
      throw createWrongNumberOfArgumentsException(1);
   }

   protected Predicate getPredicate(PTerm arg1, PTerm arg2) {
      throw createWrongNumberOfArgumentsException(2);
   }

   protected Predicate getPredicate(PTerm arg1, PTerm arg2, PTerm arg3) {
      throw createWrongNumberOfArgumentsException(3);
   }

   private IllegalArgumentException createWrongNumberOfArgumentsException(int numberOfArguments) {
      throw new IllegalArgumentException("The predicate factory: " + getClass() + " does next accept the number of arguments: " + numberOfArguments);
   }

   @Override
   public final void setKnowledgeBase(KnowledgeBase knowledgeBase) {
      this.knowledgeBase = knowledgeBase;
   }

   protected final KnowledgeBase getKnowledgeBase() {
      return knowledgeBase;
   }

   @Override
   public final boolean isRetryable() {
      return true;
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return true;
   }
}