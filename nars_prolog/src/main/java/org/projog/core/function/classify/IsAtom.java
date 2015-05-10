package org.projog.core.function.classify;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermType;

/* TEST
 %TRUE atom(abc)
 %FALSE atom(1)
 %FALSE atom(X)
 %FALSE atom(_)
 %FALSE atom(a(b,c))
 %FALSE atom([a,b,c]) 
 */
/**
 * <code>atom(X)</code> - checks that a term is an atom.
 * <p>
 * <code>atom(X)</code> succeeds if <code>X</code> currently stands for an atom.
 * </p>
 */
public final class IsAtom extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm arg) {
      return arg.type() == TermType.ATOM;
   }
}