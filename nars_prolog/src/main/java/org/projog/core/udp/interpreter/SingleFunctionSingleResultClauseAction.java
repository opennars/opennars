package org.projog.core.udp.interpreter;

import org.projog.core.KB;
import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;
import org.projog.core.udp.ClauseModel;

import java.util.Map;

/**
 * A fact whose body consists of a single non-retryable predicate.
 * <p>
 * e.g. {@code p(X) :- X<2.}
 */
public final class SingleFunctionSingleResultClauseAction extends AbstractSingleAnswerClauseAction {
   private final PTerm originalAntecedant;
   private final PredicateFactory ef;

   SingleFunctionSingleResultClauseAction(KB kb, ClauseModel ci) {
      super(kb, ci.getConsequent().terms());
      originalAntecedant = ci.getAntecedant();
      ef = kb.getPredicateFactory(originalAntecedant);
   }

   @Override
   protected boolean evaluateAntecedant(Map<PVar, PVar> sharedVariables) {
      PTerm antecedant = originalAntecedant.copy(sharedVariables);
      return ef.getPredicate(antecedant.terms()).evaluate(antecedant.terms());
   }
}