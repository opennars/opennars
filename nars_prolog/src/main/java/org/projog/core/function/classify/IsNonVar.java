package org.projog.core.function.classify;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %TRUE nonvar(abc)
 %TRUE nonvar(1)
 %TRUE nonvar(a(b,c))
 %TRUE nonvar([a,b,c])
 %QUERY X=1, nonvar(X)
 %ANSWER X=1
 %FALSE nonvar(X)
 %FALSE X=Y, nonvar(X)
 %FALSE nonvar(_)
 */
/**
 * <code>nonvar(X)</code> - checks that a term is not an uninstantiated variable.
 * <p>
 * <code>nonvar(X)</code> succeeds if <code>X</code> is not an <i>uninstantiated</i> variable.
 * </p>
 */
public final class IsNonVar extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm arg) {
      return !arg.type().isVariable();
   }
}