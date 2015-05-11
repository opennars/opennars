package org.projog.core.udp.interpreter;

import java.util.Map;

import org.projog.core.KB;
import org.projog.core.Predicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;
import org.projog.core.udp.ClauseModel;

/**
 * A functions whose body is a retryable predicate.
 * <p>
 * e.g. {@code p(X) :- repeat(X).}
 */
public final class SingleFunctionMultiResultClauseAction extends AbstractMultiAnswerClauseAction {
   private final PTerm originalAntecedant;
   private PTerm antecedant;
   private Predicate predicate;

   SingleFunctionMultiResultClauseAction(KB kb, ClauseModel ci) {
      super(kb, ci.getConsequent().terms());
      originalAntecedant = ci.getAntecedant();
   }

   private SingleFunctionMultiResultClauseAction(SingleFunctionMultiResultClauseAction original) {
      super(original);
      originalAntecedant = original.originalAntecedant;
   }

   @Override
   protected boolean evaluateAntecedant(Map<PVar, PVar> sharedVariables) {
      antecedant = originalAntecedant.copy(sharedVariables);
      predicate = kb.getPredicateFactory(antecedant).getPredicate(antecedant.terms());
      return predicate.evaluate(antecedant.terms());
   }

   @Override
   protected boolean reEvaluateAntecedant() {
      return predicate.isRetryable() && predicate.evaluate(antecedant.terms());
   }

   @Override
   public SingleFunctionMultiResultClauseAction getFree() {
      return new SingleFunctionMultiResultClauseAction(this);
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return predicate == null || predicate.couldReEvaluationSucceed();
   }
}