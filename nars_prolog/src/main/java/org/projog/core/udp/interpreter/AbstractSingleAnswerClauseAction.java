package org.projog.core.udp.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.projog.core.KB;
import org.projog.core.term.PTerm;
import org.projog.core.term.Unifier;
import org.projog.core.term.PVar;

/**
 * A clause that will not succeed more than once.
 * <p>
 * e.g. {@code p(X) :- X<2.}
 */
abstract class AbstractSingleAnswerClauseAction implements ClauseAction {
   protected final KB kb;
   private final PTerm[] originalConsequentArgs;

   AbstractSingleAnswerClauseAction(KB kb, PTerm[] consequentArgs) {
      this.kb = kb;
      this.originalConsequentArgs = consequentArgs;
   }

   @Override
   public boolean evaluate(PTerm[] queryArgs) {
      final Map<PVar, PVar> sharedVariables = new HashMap<>();
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

   protected abstract boolean evaluateAntecedant(Map<PVar, PVar> sharedVariables);

   @Override
   public AbstractSingleAnswerClauseAction getFree() {
      return this;
   }

   @Override
   public final boolean couldReEvaluationSucceed() {
      return false;
   }
}