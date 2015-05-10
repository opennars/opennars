package org.projog.core.function.classify;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %TRUE compound(a(b,c))
 %TRUE compound(1+1)
 %TRUE compound([a,b,c])
 %FALSE compound([])
 %FALSE compound(abc)
 %FALSE compound(1)
 %FALSE compound(1.5)
 %FALSE compound(X)
 %FALSE compound(_)
 */
/**
 * <code>compound(X)</code> - checks that a term is a compound term.
 * <p>
 * <code>compound(X)</code> succeeds if <code>X</code> currently stands for a compound term.
 * </p>
 */
public final class IsCompound extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm arg) {
      return arg.type().isStructure();
   }
}