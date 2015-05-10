package org.projog.core.function.math;

import static org.projog.core.KnowledgeBaseUtils.getCalculatables;

import org.projog.core.Calculatables;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Numeric;
import org.projog.core.term.PTerm;

/* TEST
 %QUERY X is 3
 %ANSWER X=3
 %QUERY X is 3+2
 %ANSWER X=5
 %QUERY X is 3.5+2.25
 %ANSWER X=5.75
 %TRUE 5 is 5
 %FALSE 5 is 6
 %TRUE 5 is 4+1
 %FALSE 5 is 4+2
 
 %QUERY X is Y
 %ERROR Cannot get Numeric for term: Y of type: NAMED_VARIABLE

 %QUERY Z=1+1, Y=9-Z, X is Y
 %ANSWER
 % X=7
 % Y=9 - (1 + 1)
 % Z = 1 + 1
 %ANSWER

 %QUERY X is _
 %ERROR Cannot get Numeric for term: _ of type: NAMED_VARIABLE

 %QUERY X is sum(1,2)
 %ERROR Cannot find calculatable: sum/2

 %QUERY X is ten
 %ERROR Cannot find calculatable: ten

 %QUERY X is []
 %ERROR Cannot get Numeric for term: [] of type: EMPTY_LIST

 %QUERY X is [1,2,3]
 %ERROR Cannot get Numeric for term: .(1, .(2, .(3, []))) of type: LIST
 */
/**
 * <code>X is Y</code> - evaluate arithmetic expression.
 * <p>
 * Firstly structure <code>Y</code> is evaluated as an arithmetic expression to give a number. Secondly an attempt is
 * made to match the number to <code>X</code>. The goal succeeds or fails based on the match.
 * </p>
 */
public final class Is extends AbstractSingletonPredicate {
   private Calculatables calculatables;

   @Override
   public void init() {
      calculatables = getCalculatables(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2) {
      Numeric n = calculatables.getNumeric(arg2);
      return arg1.unify(n);
   }
}