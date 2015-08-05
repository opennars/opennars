package org.projog.core.function.list;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

import static org.projog.core.term.ListUtils.isMember;

/* TEST
 %TRUE memberchk(a, [a,b,c])
 %TRUE memberchk(b, [a,b,c])
 %TRUE memberchk(c, [a,b,c])

 %FALSE memberchk(d, [a,b,c])
 %FALSE memberchk(d, [])
 %FALSE memberchk([], [])
 
 %QUERY memberchk(X, [a,b,c])
 %ANSWER X=a
 
 %QUERY memberchk(p(X,b), [p(a,b), p(z,Y), p(x(Y), Y)])
 %ANSWER 
 % X=a
 % Y=UNINSTANTIATED VARIABLE
 %ANSWER
*/
/**
 * <code>memberchk(E, L)</code> - checks is a term is a member of a list.
 * <p>
 * <code>memberchk(E, L)</code> succeeds if <code>E</code> is a member of the list <code>L</code>. No attempt is made to
 * retry the goal during backtracking - so if <code>E</code> appears multiple times in <code>L</code> only the first
 * occurrence will be matched.
 * </p>
 */
public final class MemberCheck extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm element, PTerm list) {
      return isMember(element, list);
   }
}
