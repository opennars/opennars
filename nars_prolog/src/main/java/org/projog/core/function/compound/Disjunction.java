package org.projog.core.function.compound;

import org.projog.core.KnowledgeBaseUtils;
import org.projog.core.Predicate;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;

/* TEST
 %QUERY true; true
 %ANSWER/
 %ANSWER/
 %TRUE_NO true; fail
 %TRUE fail; true
 %FALSE fail; fail
 
 %QUERY true; true; true
 %ANSWER/
 %ANSWER/
 %ANSWER/
 %TRUE_NO true; fail; fail
 %TRUE_NO fail; true; fail
 %TRUE fail; fail; true
 %QUERY true; true; fail
 %ANSWER/
 %ANSWER/
 %NO
 %QUERY true; fail; true
 %ANSWER/
 %ANSWER/
 %QUERY fail; true; true
 %ANSWER/
 %ANSWER/
 %FALSE fail; fail; fail

 a :- true.
 b :- true.
 c :- true.
 d :- true.
 %QUERY a;b;c
 %ANSWER/
 %ANSWER/
 %ANSWER/
 %QUERY a;b;z
 %ANSWER/
 %ANSWER/
 %NO
 %QUERY a;y;c
 %ANSWER/
 %ANSWER/
 %TRUE_NO a;y;z
 %QUERY x;b;c
 %ANSWER/
 %ANSWER/
 %TRUE_NO x;b;z
 %TRUE x;y;c
 %FALSE x;y;z

 p2(1) :- true.
 p2(2) :- true.
 p2(3) :- true.

 p3(a) :- true.
 p3(b) :- true.
 p3(c) :- true.

 p4(1, b, [a,b,c]) :- true.
 p4(3, c, [1,2,3]) :- true.
 p4(X, Y, [q,w,e,r,t,y]) :- true.

 p1(X, Y, Z) :- p2(X); p3(Y); p4(X,Y,Z).
 
 %QUERY p1(X, Y, Z)
 %ANSWER
 % X=1
 % Y=UNINSTANTIATED VARIABLE
 % Z=UNINSTANTIATED VARIABLE
 %ANSWER
 %ANSWER
 % X=2
 % Y=UNINSTANTIATED VARIABLE
 % Z=UNINSTANTIATED VARIABLE
 %ANSWER
 %ANSWER
 % X=3
 % Y=UNINSTANTIATED VARIABLE
 % Z=UNINSTANTIATED VARIABLE
 %ANSWER
 %ANSWER
 % X=UNINSTANTIATED VARIABLE
 % Y=a
 % Z=UNINSTANTIATED VARIABLE
 %ANSWER
 %ANSWER
 % X=UNINSTANTIATED VARIABLE
 % Y=b
 % Z=UNINSTANTIATED VARIABLE
 %ANSWER
 %ANSWER
 % X=UNINSTANTIATED VARIABLE
 % Y=c
 % Z=UNINSTANTIATED VARIABLE
 %ANSWER
 %ANSWER
 % X=1
 % Y=b
 % Z=[a,b,c]
 %ANSWER
 %ANSWER
 % X=3
 % Y=c
 % Z=[1,2,3]
 %ANSWER
 %ANSWER
 % X=UNINSTANTIATED VARIABLE
 % Y=UNINSTANTIATED VARIABLE
 % Z=[q,w,e,r,t,y]
 %ANSWER
 
 %QUERY p2(X); p2(X); p2(X)
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3

 %QUERY p2(X); p3(X); p2(X)
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3
 %ANSWER X=a
 %ANSWER X=b
 %ANSWER X=c
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3
 
 %QUERY X=12; X=27; X=56
 %ANSWER X=12
 %ANSWER X=27
 %ANSWER X=56
 
 %QUERY p2(X); X=12; p3(X); X=27; p2(X)
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3
 %ANSWER X=12
 %ANSWER X=a
 %ANSWER X=b
 %ANSWER X=c
 %ANSWER X=27
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3
 */
/**
 * <code>X;Y</code> - disjunction.
 * <p>
 * <code>X;Y</code> specifies a disjunction of goals. <code>X;Y</code> succeeds if either <code>X</code> succeeds
 * <i>or</i> <code>Y</code> succeeds. If <code>X</code> fails then an attempt is made to satisfy <code>Y</code>. If
 * <code>Y</code> fails the entire disjunction fails.
 * </p>
 */
public final class Disjunction extends AbstractRetryablePredicate {
   private final Predicate firstPredicate;
   private final Predicate secondPredicate;
   private int currentlyEvaluatedPredicateOrdinal;

   public Disjunction() {
      this(null, null);
   }

   private Disjunction(Predicate firstPredicate, Predicate secondPredicate) {
      this.firstPredicate = firstPredicate;
      this.secondPredicate = secondPredicate;
   }

   @Override
   public Disjunction getPredicate(PTerm arg1, PTerm arg2) {
      Predicate e1 = KnowledgeBaseUtils.getPredicate(getKB(), arg1);
      Predicate e2 = KnowledgeBaseUtils.getPredicate(getKB(), arg2);
      return new Disjunction(e1, e2);
   }

   @Override
   public boolean evaluate(PTerm inputArg1, PTerm inputArg2) {
      if (currentlyEvaluatedPredicateOrdinal == 0) {
         currentlyEvaluatedPredicateOrdinal = 1;
      } else if (currentlyEvaluatedPredicateOrdinal == 1) {
         if (!firstPredicate.isRetryable()) {
            TermUtils.backtrack(inputArg1.terms());
            currentlyEvaluatedPredicateOrdinal = 2;
         }
      } else {
         if (!secondPredicate.isRetryable()) {
            TermUtils.backtrack(inputArg2.terms());
            return false;
         }
      }

      if (currentlyEvaluatedPredicateOrdinal == 1) {
         if (firstPredicate.evaluate(inputArg1.terms())) {
            return true;
         }
         currentlyEvaluatedPredicateOrdinal = 2;
      }
      if (secondPredicate.evaluate(inputArg2.terms())) {
         return true;
      }

      return false;
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return currentlyEvaluatedPredicateOrdinal < 2 || secondPredicate.couldReEvaluationSucceed();
   }
}