package org.projog.core.function.compare;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermComparator;

/* TEST
 %FALSE b@=<a
 %TRUE b@=<b
 %TRUE b@=<c
 %FALSE b@=<1
 %TRUE b@=<b(a)
 */
/**
 * <code>X@=&lt;Y</code> - term "less than or equal" test.
 * <p>
 * Succeeds when the term argument <code>X</code> is less than or equal to the term argument <code>Y</code>.
 * </p>
 */
public final class TermLessThanOrEqual extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2) {
      return TermComparator.TERM_COMPARATOR.compare(arg1, arg2) < 1;
   }
}