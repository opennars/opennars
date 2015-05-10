package org.projog.core.udp.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.projog.core.KnowledgeBase;
import org.projog.core.term.PTerm;
import org.projog.core.term.Unifier;
import org.projog.core.term.Variable;

/**
 * A clause that will not succeed more than once.
 * <p>
 * e.g. {@code p(X) :- X<2.}
 */
abstract class AbstractSingleAnswerClauseAction implements ClauseAction {
   protected final KnowledgeBase kb;
   private final PTerm[] originalConsequentArgs;

   AbstractSingleAnswerClauseAction(KnowledgeBase kb, PTerm[] consequentArgs) {
      this.kb = kb;
      this.originalConsequentArgs = consequentArgs;
   }

   @Override
   public boolean evaluate(PTerm[] queryArgs) {
      final Map<Variable, Variable> sharedVariables = new HashMap<>();
      final PTerm[] newConsequentArgs = new PTerm[originalConsequentArgs.length];
      for (int i = 0; i < originalConsequentArgs.length; i++) {
         newConsequentArgs[i] = originalConsequentArgs[i].copy(sharedVariables);
      }

      if (Unifier.preMatch(queryArgs, newConsequentArgs) && evaluateAntecedant(sharedVariables)) {
         return true;
      } else {
         return false;
      }
   }

   protected abstract boolean evaluateAntecedant(Map<Variable, Variable> sharedVariables);

   @Override
   public AbstractSingleAnswerClauseAction getFree() {
      return this;
   }

   @Override
   public final boolean couldReEvaluationSucceed() {
      return false;
   }
}