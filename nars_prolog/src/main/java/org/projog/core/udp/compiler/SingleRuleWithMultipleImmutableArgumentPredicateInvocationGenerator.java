package org.projog.core.udp.compiler;

import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;
import org.projog.core.udp.SingleRuleWithMultipleImmutableArgumentsPredicate;

final class SingleRuleWithMultipleImmutableArgumentPredicateInvocationGenerator implements PredicateInvocationGenerator {
   @Override
   public void generate(CompiledPredicateWriter g) {
      PTerm function = g.currentClause().getCurrentFunction();
      PredicateFactory ef = g.currentClause().getCurrentPredicateFactory();

      String functionVariableName = null;
      if (g.isSpyPointsEnabled()) {
         functionVariableName = g.classVariables().getPredicateFactoryVariableName(function, g.knowledgeBase());
         g.logInlinedPredicatePredicate("Call", functionVariableName, function);
      }
      PTerm[] data = ((SingleRuleWithMultipleImmutableArgumentsPredicate) ef).data;
      Runnable r = g.createOnBreakCallback(functionVariableName, function, null);
      for (int i = 0; i < data.length; i++) {
         g.outputEqualsEvaluation(function.arg(i), data[i], r);
      }
      g.logInlinedPredicatePredicate("Exit", functionVariableName, function);
   }
}