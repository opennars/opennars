package org.projog.core.udp.compiler;

/**
 * Outputs java code that matches functionality of {@link org.projog.core.function.bool.Fail}
 */
final class FailPredicateInvocationGenerator implements PredicateInvocationGenerator {
   @Override
   public void generate(CompiledPredicateWriter w) {
      w.outputIfTrueThenBreak("true");
   }
}