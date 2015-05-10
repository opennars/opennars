package org.projog.core.udp.interpreter;

import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;

/**
 * Facts are clauses that have a body of {@code true}.
 * <p>
 * As facts have a body of {@code true} then, if the head unifies with the query, the clause will always be successfully
 * evaluated once and only once.
 * <p>
 * e.g. {@code p(a,b,c).} or {@code p :- true.}
 */
abstract class AbstractFactClauseAction implements ClauseAction {
   private final PTerm[] consequentArgs;

   AbstractFactClauseAction(PTerm[] consequentArgs) {
      this.consequentArgs = consequentArgs;
   }

   @Override
   public AbstractFactClauseAction getFree() {
      return this;
   }

   @Override
   public final boolean couldReEvaluationSucceed() {
      return false;
   }

   @Override
   public boolean evaluate(PTerm[] queryArgs) {
      return TermUtils.unify(queryArgs, consequentArgs);
   }

   protected PTerm[] getConsequentArgs() {
      return consequentArgs;
   }
}