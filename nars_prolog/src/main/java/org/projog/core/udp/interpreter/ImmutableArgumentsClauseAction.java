package org.projog.core.udp.interpreter;

import org.projog.core.term.PTerm;

/**
 * A fact that does not contain any variables.
 * <p>
 * e.g. {@code p(a,b,c).}
 */
public final class ImmutableArgumentsClauseAction extends AbstractFactClauseAction {
   ImmutableArgumentsClauseAction(PTerm[] consequentArgs) {
      super(consequentArgs);
   }
}