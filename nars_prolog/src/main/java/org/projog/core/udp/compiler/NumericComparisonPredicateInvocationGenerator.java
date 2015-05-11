package org.projog.core.udp.compiler;

import org.projog.core.term.PTerm;

/**
 * Outputs java code that matches the functionality of classes in {@code org.projog.core.function.compare}
 */
abstract class NumericComparisonPredicateInvocationGenerator implements PredicateInvocationGenerator {
   protected void ouputNumericComparison(CompiledPredicateWriter g, String logic) {
      PTerm function = g.currentClause().getCurrentFunction();
      String args = g.outputCreateTermStatement(function.term(0), true) + ", " + g.outputCreateTermStatement(function.term(1), true);
      g.setNeedsCalculatablesStaticVariable(true);
      String eval = "NUMERIC_TERM_COMPARATOR.compare(" + args + ", c)" + logic;
      // NOTE: no need to backtrack args in numeric term comparator evaluation (as no assignments made)
      // (so no need to to update currentClause.variablesToBackTrack)
      g.outputIfTrueThenBreak(eval);
   }

   /**
    * Outputs java code that matches functionality of {@link org.projog.core.function.compare.NumericEquality}
    */
   static class NumericEqualPredicateInvocationGenerator extends NumericComparisonPredicateInvocationGenerator {
      @Override
      public void generate(CompiledPredicateWriter g) {
         ouputNumericComparison(g, "!=0");
      }
   }

   /**
    * Outputs java code that matches functionality of {@link org.projog.core.function.compare.NumericInequality}
    */
   static class NumericNotEqualPredicateInvocationGenerator extends NumericComparisonPredicateInvocationGenerator {
      @Override
      public void generate(CompiledPredicateWriter g) {
         ouputNumericComparison(g, "==0");
      }
   }

   /**
    * Outputs java code that matches functionality of {@link org.projog.core.function.compare.NumericGreaterThan}
    */
   static class NumericGreaterThanPredicateInvocationGenerator extends NumericComparisonPredicateInvocationGenerator {
      @Override
      public void generate(CompiledPredicateWriter g) {
         ouputNumericComparison(g, "!=1");
      }
   }

   /**
    * Outputs java code that matches functionality of {@link org.projog.core.function.compare.NumericGreaterThanOrEqual}
    */
   static class NumericGreaterThanOrEqualPredicateInvocationGenerator extends NumericComparisonPredicateInvocationGenerator {
      @Override
      public void generate(CompiledPredicateWriter g) {
         ouputNumericComparison(g, "<0");
      }
   }

   /**
    * Outputs java code that matches functionality of {@link org.projog.core.function.compare.NumericLessThan}
    */
   static class NumericLessThanPredicateInvocationGenerator extends NumericComparisonPredicateInvocationGenerator {
      @Override
      public void generate(CompiledPredicateWriter g) {
         ouputNumericComparison(g, "!=-1");
      }
   }

   /**
    * Outputs java code that matches functionality of {@link org.projog.core.function.compare.NumericLessThanOrEqual}
    */
   static class NumericLessThanOrEqualPredicateInvocationGenerator extends NumericComparisonPredicateInvocationGenerator {
      @Override
      public void generate(CompiledPredicateWriter g) {
         ouputNumericComparison(g, ">0");
      }
   }
}