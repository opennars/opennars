package org.projog.core.function.flow;

import static org.projog.core.term.TermUtils.castToNumeric;

import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.PTerm;

/* TEST
 %QUERY repeat(3), write('hello, world'), nl
 %OUTPUT
 % hello, world
 %
 %OUTPUT
 %ANSWER/
 %OUTPUT 
 % hello, world
 %
 %OUTPUT
 %ANSWER/
 %OUTPUT 
 % hello, world
 %
 %OUTPUT
 %ANSWER/ 

 %QUERY repeat(1)
 %ANSWER/
 %QUERY repeat(2)
 %ANSWER/
 %ANSWER/
 %QUERY repeat(3)
 %ANSWER/
 %ANSWER/
 %ANSWER/
 %FALSE repeat(0)
 %FALSE repeat(-1)

 %QUERY repeat(X)
 %ERROR Expected Numeric but got: NAMED_VARIABLE with value: X
*/
/**
 * <code>repeat(N)</code> - succeeds <code>N</code> times.
 */
public final class RepeatSetAmount extends AbstractRetryablePredicate {
   private final long limit;
   private int ctr;

   public RepeatSetAmount() {
      this(0);
   }

   /**
    * Sets number of times it will successfully evaluate.
    * 
    * @param limit the number of times to successfully evaluate
    */
   public RepeatSetAmount(long limit) {
      this.limit = limit;
   }

   @Override
   public boolean evaluate(PTerm arg) {
      return evaluate();
   }

   /**
    * Public no-arg overloaded version of {@code evaluate}.
    * <p>
    * <b>Note:</b> {@code public} as this overloaded version will be called directly for static user defined predicates
    * that have a number of clauses, all of which will always evaluate successfully exactly once. (e.g. {@code a. a. a.}
    * or {@code p(). p(). p().}
    * 
    * @return {@code true} if this instance has not yet been successfully evaluated for the number of times specified
    * when it was created, else {@code false}
    * @see org.projog.core.udp.StaticUserDefinedPredicateFactory
    */
   @Override
   public boolean evaluate() {
      return ctr++ < limit;
   }

   @Override
   public RepeatSetAmount getPredicate(PTerm arg) {
      long n = castToNumeric(arg).getLong();
      return new RepeatSetAmount(n);
   }

   /**
    * Public no-arg overloaded version of {@code getPredicate}.
    * <p>
    * <b>Note:</b> {@code public} as this overloaded version will be called directly for static user defined predicates
    * that have a number of clauses, all of which will always evaluate successfully exactly once. (e.g. {@code a. a. a.}
    * or {@code p(). p(). p().}
    * 
    * @return copy of this instance
    * @see org.projog.core.udp.StaticUserDefinedPredicateFactory
    */
   @Override
   public RepeatSetAmount getPredicate() {
      return new RepeatSetAmount(limit);
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return ctr < limit;
   }
}