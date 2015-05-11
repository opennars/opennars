package org.projog.core.function.list;

import static org.projog.core.function.list.PartialApplicationUtils.apply;
import static org.projog.core.function.list.PartialApplicationUtils.createArguments;
import static org.projog.core.function.list.PartialApplicationUtils.getPredicateFactory;
import static org.projog.core.function.list.PartialApplicationUtils.isAtomOrStructure;
import static org.projog.core.function.list.PartialApplicationUtils.isList;
import static org.projog.core.term.ListFactory.createList;
import static org.projog.core.term.ListUtils.toJavaUtilList;

import java.util.ArrayList;
import java.util.List;

import org.projog.core.PredicateFactory;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %TRUE include(atom, [], [])
 %TRUE include(atom, [a], [a])
 %QUERY include(atom, [X], [])
 %ANSWER X=UNINSTANTIATED VARIABLE
 %TRUE include(atom, [1], [])
 %TRUE include(integer, [1], [1])

 %QUERY include(atom, [a,a,a], X)
 %ANSWER X=[a,a,a]

 %QUERY include(atom, [a,b,c],X)
 %ANSWER X=[a,b,c]


 %QUERY include(atom, [1,b,c], X)
 %ANSWER X=[b,c]

 %QUERY include(atom, [a,2,c], X)
 %ANSWER X=[a,c]

 %QUERY include(atom, [a,b,3], X)
 %ANSWER X=[a,b]

 %QUERY include(atom, [a,2,3], X)
 %ANSWER X=[a]

 %QUERY include(atom, [1,b,3], X)
 %ANSWER X=[b]

 %QUERY include(atom, [1,2,c], X)
 %ANSWER X=[c]

 %QUERY include(atom, [1,2,3], X)
 %ANSWER X=[]

 %TRUE include(<(0), [5,6,1,8,7,4,2,9,3], [5,6,1,8,7,4,2,9,3])
 %QUERY include(<(0), [5,6,1,8,7,4,2,9,3], X)
 %ANSWER X=[5,6,1,8,7,4,2,9,3]
 %QUERY include(>(5), [5,6,1,8,7,4,2,9,3], X)
 %ANSWER X=[1,4,2,3]
 %QUERY include(>(7), [5,6,1,8,7,4,2,9,3], X)
 %ANSWER X=[5,6,1,4,2,3]
 %TRUE include(>(7), [5,6,1,8,7,4,2,9,3], [5,6,1,4,2,3])
 %QUERY include(=(7), [5,6,1,8,7,4,2,9,3], X)
 %ANSWER X=[7]
 %QUERY include(=(0), [5,6,1,8,7,4,2,9,3], X)
 %ANSWER X=[]

 %QUERY include(=(p(W)), [p(1),p(2),p(3)], Z)
 %ANSWER
 % W=1
 % Z=[p(1)]
 %ANSWER
 %QUERY include(=(p(1,A,3)), [p(W,a,4), p(X,b,3), p(X,Y,3), p(Z,c,3)], B)
 %ANSWER
 % A=b
 % B=[p(1, b, 3),p(1, b, 3)]
 % W=UNINSTANTIATED VARIABLE
 % X=1
 % Y=b
 % Z=UNINSTANTIATED VARIABLE
 %ANSWER

 % First argument must be an atom or structure. Second argument must be a list.
 %FALSE include(X, [], Z)
 %FALSE include(atom, X, Z)

 % Note: "sublist" is a synonym for "include".
 %QUERY sublist(atom, [a,b,c], X)
 %ANSWER X=[a,b,c]
 %QUERY sublist(atom, [a,2,c], X)
 %ANSWER X=[a,c]
 */
/**
 * <code>include(X,Y,Z)</code> - filters a list by a goal.
 * <p>
 * <code>include(X,Y,Z)</code> succeeds if the list <code>Z</code> consists of the elements of the list <code>Y</code>
 * for which the goal <code>X</code> can be successfully applied.
 * </p>
 */
public final class SubList extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm partiallyAppliedFunction, PTerm args, PTerm filteredOutput) {
      if (!isValidArguments(partiallyAppliedFunction, args)) {
         return false;
      }

      final List<PTerm> matches = new ArrayList<>();
      final PredicateFactory pf = getPredicateFactory(getKB(), partiallyAppliedFunction);
      for (PTerm arg : toJavaUtilList(args)) {
         if (apply(pf, createArguments(partiallyAppliedFunction, arg))) {
            matches.add(arg);
         }
      }
      return filteredOutput.unify(createList(matches));
   }

   private boolean isValidArguments(PTerm partiallyAppliedFunction, PTerm arg) {
      return isAtomOrStructure(partiallyAppliedFunction) && isList(arg);
   }
}
