package org.projog.core.udp.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.projog.core.KB;
import org.projog.core.term.PTerm;
import org.projog.core.term.Unifier;
import org.projog.core.term.PVar;

/**
 * A clause that can succeed more than once.
 * <p>
 * e.g. {@code p(X) :- repeat(X).}
 */
abstract class AbstractMultiAnswerClauseAction implements ClauseAction {
   protected KB kb;
   private final PTerm[] originalConsequentArgs;
   private Map<PVar, PVar> sharedVariables;

   AbstractMultiAnswerClauseAction(KB kb, PTerm[] consequentArgs) {
      this.kb = kb;
      this.originalConsequentArgs = consequentArgs;
   }

   AbstractMultiAnswerClauseAction(AbstractMultiAnswerClauseAction action) {
      this(action.kb, action.originalConsequentArgs);
   }

   @Override
   public boolean evaluate(PTerm[] queryArgs) {
      if (sharedVariables == null) {
         sharedVariables = new HashMap<>();
         PTerm[] consequentArgs = new PTerm[originalConsequentArgs.length];
         for (int i = 0; i < consequentArgs.length; i++) {
            consequentArgs[i] = originalConsequentArgs[i].copy(sharedVariables);
         }
         boolean matched = Unifier.preMatch(queryArgs, consequentArgs);
         if (matched) {
            return evaluateAntecedant(sharedVariables);
         } else {
            return false;
         }
      } else {
         return reEvaluateAntecedant();
      }
   }

   protected abstract boolean evaluateAntecedant(Map<PVar, PVar> sharedVariables);

   protected abstract boolean reEvaluateAntecedant();
}