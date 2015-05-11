package org.projog.core.udp.compiler;

import static org.projog.core.udp.compiler.CompiledPredicateVariables.ARGUMENT_PREFIX;
import static org.projog.core.udp.compiler.CompiledPredicateVariables.PLACEHOLDER_PREFIX;

import org.projog.core.term.PTerm;

final class RecursivePredicateInvocationGenerator implements PredicateInvocationGenerator {
   @Override
   public void generate(CompiledPredicateWriter g) {
      PTerm function = g.currentClause().getCurrentFunction();

      if (g.factMetaData().isTailRecursive() && g.currentClause().getConjunctionIndex() == g.currentClause().getConjunctionCount() - 1) {
         for (int i = 0; i < g.factMetaData().getNumberArguments(); i++) {
            PTerm tailRecursiveArgument = function.term(i);
            String tailRecursiveArgumentSyntax = g.outputCreateTermStatement(tailRecursiveArgument, true);
            if (g.factMetaData().isTailRecursiveArgument(i)) {
               g.assign(ARGUMENT_PREFIX + i, tailRecursiveArgumentSyntax + "==null?" + PLACEHOLDER_PREFIX + i + ":" + tailRecursiveArgumentSyntax + ".getTerm()");
            } else {
               g.assign(ARGUMENT_PREFIX + i, tailRecursiveArgumentSyntax + ".getTerm()");
            }
         }
      } else {
         g.callUserDefinedPredicate(g.className(), true);
      }
   }
}