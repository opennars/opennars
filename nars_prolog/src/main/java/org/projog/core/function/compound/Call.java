package org.projog.core.function.compound;

import org.projog.core.KnowledgeBase;
import org.projog.core.KnowledgeBaseUtils;
import org.projog.core.Predicate;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.PTerm;

/* TEST
 %TRUE call(true)
 %FALSE call(fail)
 %QUERY X = true, call(X)
 %ANSWER X = true
 %FALSE X = fail, call(X)

 test(a).
 test(b).
 test(c).

 %QUERY X = test(Y), call(X)
 %ANSWER
 % X = test(a)
 % Y = a
 %ANSWER
 %ANSWER
 % X = test(b)
 % Y = b
 %ANSWER
 %ANSWER
 % X = test(c)
 % Y = c
 %ANSWER
 
 testCall(X) :- call(X).
 
 %FALSE testCall(fail)
 %TRUE testCall(true)
 %QUERY testCall((true ; true))
 %ANSWER/
 %ANSWER/
 
 % Note: "time" is a synonym for "call".
 %TRUE time(true)
 %FALSE time(fail)
 %QUERY time(repeat(3))
 %ANSWER/
 %ANSWER/
 %ANSWER/
 */
/**
 * <code>call(X)</code> - calls the goal represented by a term.
 * <p>
 * The predicate <code>call</code> makes it possible to call goals that are determined at runtime rather than when a
 * program is written. <code>call(X)</code> succeeds if the goal represented by the term <code>X</code> succeeds.
 * <code>call(X)</code> fails if the goal represented by the term <code>X</code> fails. An attempt is made to retry the
 * goal during backtracking.
 * </p>
 */
public final class Call extends AbstractRetryablePredicate {
   private Predicate predicateToCall;
   private PTerm[] argumentsForPredicateToCall;

   /** needed to create prototype actual instances can be created from */
   public Call() {
   }

   private Call(KnowledgeBase kb) {
      setKnowledgeBase(kb);
   }

   @Override
   public Call getPredicate(PTerm arg) {
      return new Call(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(PTerm t) {
      if (predicateToCall == null) {
         predicateToCall = KnowledgeBaseUtils.getPredicate(getKnowledgeBase(), t);
         argumentsForPredicateToCall = t.getArgs();
      } else if (predicateToCall.isRetryable() == false) {
         return false;
      }

      return predicateToCall.evaluate(argumentsForPredicateToCall);
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return predicateToCall == null || predicateToCall.couldReEvaluationSucceed();
   }
}