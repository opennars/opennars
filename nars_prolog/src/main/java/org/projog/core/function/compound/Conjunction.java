package org.projog.core.function.compound;

import java.util.HashMap;

import org.projog.core.KB;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;
import org.projog.core.term.Unifier;
import org.projog.core.term.PVar;

/* TEST
 %TRUE true, true
 %FALSE true, fail
 %FALSE fail, true
 %FALSE fail, fail
 
 %TRUE true, true, true
 %FALSE true, fail, fail
 %FALSE fail, true, fail
 %FALSE fail, fail, true
 %FALSE true, true, fail
 %FALSE true, fail, true 
 %FALSE fail, true, true
 %FALSE fail, fail, fail
 
 b :- true.
 c :- true.
 d :- true.
 y :- true.
 a :- b,c,d.
 x :- y,z.
 %TRUE a
 %FALSE x

 p2(1) :- true.
 p2(2) :- true.
 p2(3) :- true.

 p3(a) :- true.
 p3(b) :- true.
 p3(c) :- true.

 p4(1, b, [a,b,c]) :- true.
 p4(3, c, [1,2,3]) :- true.
 p4(X, Y, [q,w,e,r,t,y]) :- true.

 p1(X, Y, Z) :- p2(X), p3(Y), p4(X,Y,Z).
 
 %QUERY p1(X, Y, Z)
 %ANSWER
 % X=1
 % Y=a
 % Z=[q,w,e,r,t,y]
 %ANSWER
 %ANSWER
 % X=1
 % Y=b
 % Z=[a,b,c]
 %ANSWER
 %ANSWER
 % X=1
 % Y=b
 % Z=[q,w,e,r,t,y]
 %ANSWER
 %ANSWER
 % X=1
 % Y=c
 % Z=[q,w,e,r,t,y]
 %ANSWER
 %ANSWER
 % X=2
 % Y=a
 % Z=[q,w,e,r,t,y]
 %ANSWER
 %ANSWER
 % X=2
 % Y=b
 % Z=[q,w,e,r,t,y]
 %ANSWER
 %ANSWER
 % X=2
 % Y=c
 % Z=[q,w,e,r,t,y]
 %ANSWER
 %ANSWER
 % X=3
 % Y=a
 % Z=[q,w,e,r,t,y]
 %ANSWER
 %ANSWER
 % X=3
 % Y=b
 % Z=[q,w,e,r,t,y]
 %ANSWER
 %ANSWER
 % X=3
 % Y=c
 % Z=[1,2,3]
 %ANSWER
 %ANSWER
 % X=3
 % Y=c
 % Z=[q,w,e,r,t,y]
 %ANSWER
 
 %QUERY p2(X), p2(X), p2(X)
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3

 %FALSE p2(X), p3(X), p2(X)
 */
/**
 * <code>X,Y</code> - conjunction.
 * <p>
 * <code>X,Y</code> specifies a conjunction of goals. <code>X,Y</code> succeeds if <code>X</code> succeeds <i>and</i>
 * <code>Y</code> succeeds. If <code>X</code> succeeds and <code>Y</code> fails then an attempt is made to re-satisfy
 * <code>X</code>. If <code>X</code> fails the entire conjunction fails.
 * </p>
 */
public final class Conjunction extends AbstractRetryablePredicate {
   // TODO test using a junit test rather than just a Prolog script
   // as over complexity in internal workings (e.g. when and what it backtracks)
   // may not be detectable via a system test. 

   private PredicateFactory secondPredicateFactory;
   private Predicate firstPredicate;
   private Predicate secondPredicate;
   private boolean firstGo = true;
   private PTerm secondArg;
   private PTerm tmpInputArg2;

   public Conjunction() {
   }

   private Conjunction(KB KB) {
      setKB(KB);
   }

   @Override
   public Conjunction getPredicate(PTerm arg1, PTerm arg2) {
      return new Conjunction(getKB());
   }

   @Override
   public boolean evaluate(PTerm inputArg1, PTerm inputArg2) {
      if (firstGo) {
         firstPredicate = getKB().getPredicateFactory(inputArg1).getPredicate(inputArg1.terms());

         while ((firstGo || firstPredicate.isRetryable()) && firstPredicate.evaluate(inputArg1.terms())) {
            firstGo = false;
            if (preMatch(inputArg2) && secondPredicate.evaluate(secondArg.terms())) {
               return true;
            }
            TermUtils.backtrack(tmpInputArg2.terms());
         }

         return false;
      }

      do {
         final boolean evaluateSecondPredicate;
         if (secondArg == null) {
            evaluateSecondPredicate = preMatch(inputArg2);
         } else {
            evaluateSecondPredicate = secondPredicate.isRetryable();
         }

         if (evaluateSecondPredicate && secondPredicate.evaluate(secondArg.terms())) {
            return true;
         }

         TermUtils.backtrack(tmpInputArg2.terms());
         secondArg = null;
      } while (firstPredicate.isRetryable() && firstPredicate.evaluate(inputArg1.terms()));

      return false;
   }

   private boolean preMatch(PTerm inputArg2) {
      tmpInputArg2 = inputArg2.get();
      secondArg = tmpInputArg2.copy(new HashMap<PVar, PVar>());
      if (Unifier.preMatch(tmpInputArg2.terms(), secondArg.terms())) {
         if (secondPredicateFactory == null) {
            secondPredicateFactory = getKB().getPredicateFactory(secondArg);
         }
         secondPredicate = secondPredicateFactory.getPredicate(secondArg.terms());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return firstPredicate == null || firstPredicate.couldReEvaluationSucceed() || secondPredicate == null || secondPredicate.couldReEvaluationSucceed();
   }
}