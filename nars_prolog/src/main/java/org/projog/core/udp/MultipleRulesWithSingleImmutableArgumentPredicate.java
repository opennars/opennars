package org.projog.core.udp;

import org.projog.core.Predicate;
import org.projog.core.SpyPoints;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;

/**
 * Provides an optimised implementation for evaluating a particular subset of user defined predicates that have an arity
 * of one and a number of clauses that all have a body of {@code true} and no shared variables. Example:
 * 
 * <pre>
 * p(a).
 * p(b).
 * p(c).
 * </pre>
 * 
 * @see SingleRuleWithSingleImmutableArgumentPredicate
 * @see SingleRuleWithMultipleImmutableArgumentsPredicate
 * @see MultipleRulesWithMultipleImmutableArgumentsPredicate
 */
public final class MultipleRulesWithSingleImmutableArgumentPredicate extends AbstractRetryablePredicate {
   /** Public so can be used directly be code compiled at runtime. */
   public final PTerm[] data;
   /** Public so can be used directly be code compiled at runtime. */
   public final SpyPoints.SpyPoint spyPoint;
   private final boolean isDebugEnabled;
   private final int numClauses;
   private int ctr;
   private boolean retrying;

   public MultipleRulesWithSingleImmutableArgumentPredicate(PTerm[] data, SpyPoints.SpyPoint spyPoint) {
      this.data = data;
      this.numClauses = data.length;
      this.spyPoint = spyPoint;
      this.isDebugEnabled = spyPoint != null && spyPoint.isEnabled();
   }

   @Override
   public Predicate getPredicate(PTerm... args) {
      return new MultipleRulesWithSingleImmutableArgumentPredicate(data, spyPoint);
   }

   @Override
   public boolean evaluate(PTerm... args) {
      if (retrying) {
         if (isDebugEnabled) {
            spyPoint.logRedo(this, args);
         }
         TermUtils.backtrack(args);
      } else {
         if (isDebugEnabled) {
            spyPoint.logCall(this, args);
         }
         retrying = true;
      }
      while (ctr < numClauses) {
         if (args[0].unify(data[ctr++])) {
            if (isDebugEnabled) {
               spyPoint.logExit(this, args);
            }
            return true;
         } else {
            args[0].backtrack();
         }
      }
      if (isDebugEnabled) {
         spyPoint.logFail(this, args);
      }
      return false;
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return ctr < numClauses;
   }
}