package org.projog.core.function.compare;

import static org.projog.core.KnowledgeBaseUtils.getCalculatables;
import static org.projog.core.term.NumericTermComparator.NUMERIC_TERM_COMPARATOR;

import org.projog.core.Calculatables;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

abstract class AbstractNumericComparisonPredicate extends AbstractSingletonPredicate {
   private Calculatables calculatables;

   @Override
   public final void init() {
      calculatables = getCalculatables(getKnowledgeBase());
   }

   protected int compare(PTerm arg1, PTerm arg2) {
      return NUMERIC_TERM_COMPARATOR.compare(arg1, arg2, calculatables);
   }
}