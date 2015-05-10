package org.projog.core.udp.compiler;

import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.getClassNameMinusPackage;

import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;

final class CompiledPredicateInvocationGenerator implements PredicateInvocationGenerator {
   @Override
   public void generate(CompiledPredicateWriter g) {
      PTerm function = g.currentClause().getCurrentFunction();
      PredicateFactory ef = g.currentClause().getCurrentPredicateFactory();

      boolean isRetryable = ef.getPredicate(function.getArgs()).isRetryable();
      CompiledPredicate compiledPredicate = (CompiledPredicate) ef;
      String compiledPredicateName = getClassNameMinusPackage(compiledPredicate);
      g.callUserDefinedPredicate(compiledPredicateName, isRetryable);
   }
}