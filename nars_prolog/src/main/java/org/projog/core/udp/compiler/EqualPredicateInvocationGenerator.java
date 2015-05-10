package org.projog.core.udp.compiler;

import java.util.Set;

import org.projog.core.term.Variable;

/**
 * Outputs java code that matches functionality of {@link org.projog.core.function.compare.Equal}
 */
final class EqualPredicateInvocationGenerator implements PredicateInvocationGenerator {
   @Override
   public void generate(CompiledPredicateWriter g) {
      Set<Variable> variables = g.currentClause().getVariablesInCurrentFunction();
      g.currentClause().addVariablesToBackTrack(variables);
      g.outputEqualsEvaluation();
   }
}