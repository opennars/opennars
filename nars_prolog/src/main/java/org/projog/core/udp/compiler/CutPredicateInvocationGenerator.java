package org.projog.core.udp.compiler;

/**
 * Outputs java code that matches functionality of {@link org.projog.core.function.flow.Cut}
 */
final class CutPredicateInvocationGenerator implements PredicateInvocationGenerator {
   @Override
   public void generate(CompiledPredicateWriter g) {
      if (g.factMetaData().isSingleResultPredicate() == false && g.currentClause().isSingleResult() == true) {
         g.assignTrue("isCut");
      } else if (g.currentClause().isAfterLastMulipleResultConjuction()) {
         g.assignTrue("isCut");
      }
   }
}