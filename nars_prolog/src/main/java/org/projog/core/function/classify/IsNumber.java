package org.projog.core.function.classify;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %TRUE number(1)
 %TRUE number(-1)
 %TRUE number(0)
 %TRUE number(0.0)
 %TRUE number(1.0)
 %TRUE number(-1.0)
 %FALSE number('1')
 %FALSE number('1.0')
 %FALSE number(1+1)
 %FALSE number(a)
 %FALSE number(p(1,2,3))
 %FALSE number([1,2,3])
 %FALSE number([a,b,c]) 
 %FALSE number([])
 %FALSE number(X)
 %FALSE number(_)
 */
/**
 * <code>number(X)</code> - checks that a term is numeric.
 * <p>
 * <code>number(X)</code> succeeds if <code>X</code> currently stands for a number.
 * </p>
 */
public final class IsNumber extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm arg) {
      return arg.type().isNumeric();
   }
}