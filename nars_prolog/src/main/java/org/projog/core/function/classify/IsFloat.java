package org.projog.core.function.classify;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.PrologOperator;

/* TEST
 %TRUE float(1.0)
 %TRUE float(-1.0)
 %TRUE float(0.0)
 %FALSE float(1)
 %FALSE float(-1)
 %FALSE float(0)
 %FALSE float('1')
 %FALSE float('1.0')
 %FALSE float(a)
 %FALSE float(p(1.0,2.0,3.0))
 %FALSE float([1.0,2.0,3.0])
 %FALSE float([])
 %FALSE float(X)
 %FALSE float(_)
*/
/**
 * <code>float(X)</code> - checks that a term is a floating point number.
 * <p>
 * <code>float(X)</code> succeeds if <code>X</code> currently stands for a floating point number.
 * </p>
 */
public final class IsFloat extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm arg) {
      return arg.type() == PrologOperator.FRACTION;
   }
}