package org.projog.core.function.classify;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermType;

/* TEST
 %TRUE is_list([1.0,2.0,3.0])
 %TRUE is_list([])
 %TRUE is_list([a|[]])

 %FALSE is_list([a|b])
 %FALSE is_list([a|X])
 %FALSE is_list(X)
 */
/**
 * <code>is_list(X)</code> - checks that a term is a list.
 * <p>
 * <code>is_list(X)</code> succeeds if <code>X</code> currently stands for a
 * list.
 * </p>
 */
public final class IsList extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(final PTerm arg) {
      switch (arg.type()) {
         case EMPTY_LIST:
            return true;
         case LIST:
            PTerm tail = arg;
            while ((tail = tail.arg(1)).type() == TermType.LIST) {
            }
            return tail.type() == TermType.EMPTY_LIST;
         default:
            return false;
      }
   }
}