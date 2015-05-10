package org.projog.core.udp.interpreter;

import org.projog.core.term.PTerm;

/**
 * Defines a fact that will always be true.
 * <p>
 * e.g. {@code e.g. p(X,Y,Z).}
 */
public final class AlwaysMatchedClauseAction extends AbstractFactClauseAction {
   AlwaysMatchedClauseAction(PTerm[] consequentArgs) {
      super(consequentArgs);
   }

   @Override
   public boolean evaluate(PTerm[] queryArgs) {
      return true;
   }
}