package org.projog.core.udp.interpreter;

import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;
import org.projog.core.term.Unifier;
import org.projog.core.udp.TailRecursivePredicate;
import org.projog.core.udp.TailRecursivePredicateMetaData;

import java.util.HashMap;
import java.util.Map;

/**
 * A implementation of {@link TailRecursivePredicate} for interpreted user defined predicates.
 * <p>
 * The user defined predicate must be judged as eligible for <i>tail recursion optimisation</i> using the criteria used
 * by {@link TailRecursivePredicateMetaData}.
 * </p>
 * <img src="doc-files/InterpretedTailRecursivePredicateFactory.png">
 * 
 * @see InterpretedTailRecursivePredicateFactory
 * @see TailRecursivePredicateMetaData
 */
final class InterpretedTailRecursivePredicate extends TailRecursivePredicate {
   // TODO doesn't currently respect spypoints

   private final int numArgs;
   private final PTerm[] currentQueryArgs;
   private final boolean isRetryable;
   private final PredicateFactory[] firstClausePredicateFactories;
   private final PTerm[] firstClauseConsequentArgs;
   private final PTerm[] firstClauseOriginalTerms;
   private final PredicateFactory[] secondClausePredicateFactories;
   private final PTerm[] secondClauseConsequentArgs;
   private final PTerm[] secondClauseOriginalTerms;

   InterpretedTailRecursivePredicate(PTerm[] inputArgs, PredicateFactory[] firstClausePredicateFactories, PTerm[] firstClauseConsequentArgs, PTerm[] firstClauseOriginalTerms, PredicateFactory[] secondClausePredicateFactories,
            PTerm[] secondClauseConsequentArgs, PTerm[] secondClauseOriginalTerms, boolean isRetryable) {
      this.numArgs = inputArgs.length;
      this.currentQueryArgs = new PTerm[numArgs];
      for (int i = 0; i < numArgs; i++) {
         currentQueryArgs[i] = inputArgs[i].get();
      }

      this.firstClausePredicateFactories = firstClausePredicateFactories;
      this.firstClauseConsequentArgs = firstClauseConsequentArgs;
      this.firstClauseOriginalTerms = firstClauseOriginalTerms;
      this.secondClausePredicateFactories = secondClausePredicateFactories;
      this.secondClauseConsequentArgs = secondClauseConsequentArgs;
      this.secondClauseOriginalTerms = secondClauseOriginalTerms;
      this.isRetryable = isRetryable;
   }

   @Override
   protected boolean matchFirstRule() {
      final Map<PVar, PVar> sharedVariables = new HashMap<>();
      final PTerm[] newConsequentArgs = new PTerm[numArgs];
      for (int i = 0; i < numArgs; i++) {
         newConsequentArgs[i] = firstClauseConsequentArgs[i].copy(sharedVariables);
      }

      if (Unifier.preMatch(currentQueryArgs, newConsequentArgs) == false) {
         return false;
      }

      for (int i = 0; i < firstClauseOriginalTerms.length; i++) {
         PTerm t = firstClauseOriginalTerms[i].copy(sharedVariables);
         if (!firstClausePredicateFactories[i].getPredicate(t.terms()).evaluate(t.terms())) {
            return false;
         }
      }

      return true;
   }

   @Override
   protected boolean matchSecondRule() {
      final Map<PVar, PVar> sharedVariables = new HashMap<>();
      final PTerm[] newConsequentArgs = new PTerm[numArgs];
      for (int i = 0; i < numArgs; i++) {
         newConsequentArgs[i] = secondClauseConsequentArgs[i].copy(sharedVariables);
      }

      if (Unifier.preMatch(currentQueryArgs, newConsequentArgs) == false) {
         return false;
      }

      for (int i = 0; i < secondClauseOriginalTerms.length - 1; i++) {
         PTerm t = secondClauseOriginalTerms[i].copy(sharedVariables);
         if (!secondClausePredicateFactories[i].getPredicate(t.terms()).evaluate(t.terms())) {
            return false;
         }
      }

      PTerm finalTermArgs[] = secondClauseOriginalTerms[secondClauseOriginalTerms.length - 1].terms();
      for (int i = 0; i < numArgs; i++) {
         currentQueryArgs[i] = finalTermArgs[i].copy(sharedVariables);
      }

      return true;
   }

   @Override
   protected void backtrack() {
      for (int i = 0; i < numArgs; i++) {
         currentQueryArgs[i].backtrack();
      }
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return isRetryable;
   }

   @Override
   public boolean isRetryable() {
      return isRetryable;
   }
}