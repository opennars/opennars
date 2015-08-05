package org.projog.core.udp.compiler;

import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;

import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.getClassNameMinusPackage;

final class CompiledPredicateInvocationGenerator implements PredicateInvocationGenerator {
   @Override
   public void generate(CompiledPredicateWriter g) {
      PTerm function = g.currentClause().getCurrentFunction();
      PredicateFactory ef = g.currentClause().getCurrentPredicateFactory();

      boolean isRetryable = ef.getPredicate(function.terms()).isRetryable();
      CompiledPredicate compiledPredicate = (CompiledPredicate) ef;
      String compiledPredicateName = getClassNameMinusPackage(compiledPredicate);
      g.callUserDefinedPredicate(compiledPredicateName, isRetryable);
   }
}