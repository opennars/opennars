package org.projog.core.udp.interpreter;

import java.util.Map;

import org.projog.core.KnowledgeBase;
import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;
import org.projog.core.term.Variable;
import org.projog.core.udp.ClauseModel;

/**
 * A fact whose body consists of a single non-retryable predicate.
 * <p>
 * e.g. {@code p(X) :- X<2.}
 */
public final class SingleFunctionSingleResultClauseAction extends AbstractSingleAnswerClauseAction {
   private final PTerm originalAntecedant;
   private final PredicateFactory ef;

   SingleFunctionSingleResultClauseAction(KnowledgeBase kb, ClauseModel ci) {
      super(kb, ci.getConsequent().getArgs());
      originalAntecedant = ci.getAntecedant();
      ef = kb.getPredicateFactory(originalAntecedant);
   }

   @Override
   protected boolean evaluateAntecedant(Map<Variable, Variable> sharedVariables) {
      PTerm antecedant = originalAntecedant.copy(sharedVariables);
      return ef.getPredicate(antecedant.getArgs()).evaluate(antecedant.getArgs());
   }
}