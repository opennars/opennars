package org.projog.core.function.compare;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermComparator;

/* TEST
 %TRUE b@>=a
 %TRUE b@>=b
 %FALSE b@>=c
 %TRUE b@>=1
 %FALSE b@>=b(a)
 */
/**
 * <code>X@&gt;=Y</code> - term "greater than or equal" test.
 * <p>
 * Succeeds when the term argument <code>X</code> is greater than or equal to the term argument <code>Y</code>.
 * </p>
 */
public final class TermGreaterThanOrEqual extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2) {
      return TermComparator.TERM_COMPARATOR.compare(arg1, arg2) > -1;
   }
}