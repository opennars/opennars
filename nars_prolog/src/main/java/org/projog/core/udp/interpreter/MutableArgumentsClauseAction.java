package org.projog.core.udp.interpreter;

import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;

/**
 * A fact (works with variable arguments).
 * <p>
 * e.g. {@code p(a,Y,Z).} or {@code p(X,Y,X).}
 */
public final class MutableArgumentsClauseAction extends AbstractFactClauseAction {
   MutableArgumentsClauseAction(PTerm[] consequentArgs) {
      super(consequentArgs);
   }

   @Override
   public MutableArgumentsClauseAction getFree() {
      PTerm[] newConsequentArgs = TermUtils.copy(getConsequentArgs());
      return new MutableArgumentsClauseAction(newConsequentArgs);
   }
}