package org.projog.core.udp.interpreter;

import java.util.Map;

import org.projog.core.KnowledgeBase;
import org.projog.core.Predicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.Variable;
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

   SingleFunctionMultiResultClauseAction(KnowledgeBase kb, ClauseModel ci) {
      super(kb, ci.getConsequent().getArgs());
      originalAntecedant = ci.getAntecedant();
   }

   private SingleFunctionMultiResultClauseAction(SingleFunctionMultiResultClauseAction original) {
      super(original);
      originalAntecedant = original.originalAntecedant;
   }

   @Override
   protected boolean evaluateAntecedant(Map<Variable, Variable> sharedVariables) {
      antecedant = originalAntecedant.copy(sharedVariables);
      predicate = kb.getPredicateFactory(antecedant).getPredicate(antecedant.getArgs());
      return predicate.evaluate(antecedant.getArgs());
   }

   @Override
   protected boolean reEvaluateAntecedant() {
      return predicate.isRetryable() && predicate.evaluate(antecedant.getArgs());
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