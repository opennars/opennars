package org.projog.core.function.list;

import org.projog.core.ProjogException;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.PrologOperator;

/* TEST
 %TRUE_NO member(a, [a,b,c])
 %TRUE_NO member(b, [a,b,c])
 %TRUE member(c, [a,b,c])
 
 %FALSE member(d, [a,b,c])
 %FALSE member(d, [])
 %FALSE member([], [])

 %QUERY member(X, [a,b,c])
 %ANSWER X=a
 %ANSWER X=b
 %ANSWER X=c
 
 %QUERY member(p(X,b), [p(a,b), p(z,Y), p(x(Y), Y)])
 %ANSWER 
 % X=a
 % Y=UNINSTANTIATED VARIABLE
 %ANSWER
 %ANSWER 
 % X=z
 % Y=b
 %ANSWER
 %ANSWER 
 % X=x(b)
 % Y=b
 %ANSWER
 */
/**
 * <code>member(E, L)</code> - enumerates members of a list.
 * <p>
 * <code>member(E, L)</code> succeeds if <code>E</code> is a member of the list <code>L</code>. An attempt is made to
 * retry the goal during backtracking - so it can be used to enumerate the members of a list.
 * </p>
 */
public final class Member extends AbstractRetryablePredicate {
   private PTerm list;

   @Override
   public Member getPredicate(PTerm element, PTerm list) {
      final Member m = new Member();
      if (list.type() != PrologOperator.LIST && list.type() != PrologOperator.EMPTY_LIST) {
         throw new ProjogException("Expected list but got: " + list);
      }
      m.list = list;
      return m;
   }

   @Override
   public boolean evaluate(PTerm element, PTerm secondArg) {
      while (true) {
         if (couldReEvaluationSucceed()) {
            element.backtrack();
            secondArg.backtrack();
            PTerm head = list.term(0);
            list = list.term(1);
            if (element.unify(head)) {
               return true;
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return list.type() == PrologOperator.LIST;
   }
}
