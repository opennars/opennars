package org.projog.core.udp;

import org.projog.core.SpyPoints;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;

/**
 * Provides an optimised implementation for evaluating a particular subset of user defined predicates that have an arity
 * greater than one and a single clause that has a body of {@code true} and no shared variables. Example:
 * 
 * <pre>
 * p(a,b,c).
 * </pre>
 * 
 * @see SingleRuleWithSingleImmutableArgumentPredicate
 * @see MultipleRulesWithSingleImmutableArgumentPredicate
 * @see MultipleRulesWithMultipleImmutableArgumentsPredicate
 */
public final class SingleRuleWithMultipleImmutableArgumentsPredicate extends AbstractSingletonPredicate {
   /** Public so can be used directly be code compiled at runtime. */
   public final PTerm[] data;
   /** Public so can be used directly be code compiled at runtime. */
   public final SpyPoints.SpyPoint spyPoint;

   public SingleRuleWithMultipleImmutableArgumentsPredicate(PTerm[] data, SpyPoints.SpyPoint spyPoint) {
      this.data = data;
      this.spyPoint = spyPoint;
   }

   @Override
   public boolean evaluate(PTerm... args) {
      if (spyPoint != null) {
         spyPoint.logCall(this, args);
      }
      final boolean result = TermUtils.unify(args, data);
      if (result) {
         if (spyPoint != null) {
            spyPoint.logExit(this, args);
         }
      } else {
         if (spyPoint != null) {
            spyPoint.logFail(this, args);
         }
      }
      return result;
   }
}