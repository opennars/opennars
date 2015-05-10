package org.projog.core.function.list;

import static org.projog.core.function.list.PartialApplicationUtils.apply;
import static org.projog.core.function.list.PartialApplicationUtils.createArguments;
import static org.projog.core.function.list.PartialApplicationUtils.getPredicateFactory;
import static org.projog.core.function.list.PartialApplicationUtils.isAtomOrStructure;
import static org.projog.core.function.list.PartialApplicationUtils.isList;
import static org.projog.core.term.ListFactory.createListOfLength;
import static org.projog.core.term.ListUtils.toJavaUtilList;

import java.util.List;

import org.projog.core.PredicateFactory;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %TRUE maplist(atom, [])
 %TRUE maplist(atom, [a])
 %FALSE maplist(atom, [X])
 %FALSE maplist(atom, [1])
 %TRUE maplist(integer, [1])

 %TRUE maplist(atom, [a,a,a])
 %TRUE maplist(atom, [a,b,c])
 %FALSE maplist(atom, [1,b,c])
 %FALSE maplist(atom, [a,2,c])
 %FALSE maplist(atom, [a,b,3])
 %FALSE maplist(atom, [a,2,3])
 %FALSE maplist(atom, [1,b,3])
 %FALSE maplist(atom, [1,2,c])
 %FALSE maplist(atom, [1,2,3])

 %FALSE maplist(>(0), [3,4,2,1])
 %FALSE maplist(<(5), [3,4,2,1])
 %TRUE maplist(<(0), [3,4,2,1])
 %TRUE maplist(>(5), [3,4,2,1])
 %FALSE maplist(>(5), [3,4,5,2,1])
 %TRUE maplist(>=(5), [3,4,5,2,1])
 %FALSE maplist(>=(5), [3,4,5,2,1,6])
 %FALSE maplist(>=(5), [6,3,4,5,2,1])
 %FALSE maplist(>=(5), [3,4,5,6,2,1])
 
 %FALSE maplist(=(p(W)), [p(1),p(2),p(3)])
 
 % First argument must be an atom or structure. Second argument must be a list.
 %FALSE maplist(X, [])
 %FALSE maplist(atom, X)

 % maplist/3 applies the goal to pairs of elements from two lists.
 %TRUE maplist(=, [1,2,3], [1,2,3])
 %FALSE maplist(=, [1,2,3], [4,5,6])
 %FALSE maplist(=, [1,2,3], [1,3,2]) 
 %QUERY maplist(=, [X,2,3], [1,Y,Z])
 %ANSWER
 % X=1
 % Y=2
 % Z=3
 %ANSWER
 %QUERY maplist(=, [1,2,3], X)
 %ANSWER X=[1,2,3]
 
 % Note: "checklist" is a synonym for "maplist".
 %TRUE checklist(atom, [a,b,c])
 %FALSE checklist(atom, [a,2,c])
 %TRUE checklist(=, [1,2,3], [1,2,3])
 %FALSE checklist(=, [1,2,3], [4,5,6])
 */
/**
 * <code>maplist(X,Y)</code> / <code>maplist(X,Y,Z)</code> - determines if a goal succeeds against elements of a list.
 * <p>
 * <code>maplist(X,Y)</code> succeeds if the goal <code>X</code> can be successfully applied to each elements of the
 * list <code>Y</code>.
 * </p>
 */
public final class MapList extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm partiallyAppliedFunction, PTerm args) {
      if (!isValidArguments(partiallyAppliedFunction, args)) {
         return false;
      }

      final PredicateFactory pf = getPredicateFactory(getKnowledgeBase(), partiallyAppliedFunction);
      for (PTerm arg : toJavaUtilList(args)) {
         if (!apply(pf, createArguments(partiallyAppliedFunction, arg))) {
            return false;
         }
      }
      return true;
   }

   @Override
   public boolean evaluate(PTerm partiallyAppliedFunction, PTerm args1, PTerm args2) {
      if (!isAtomOrStructure(partiallyAppliedFunction)) {
         return false;
      }

      List<PTerm> list1;
      List<PTerm> list2;
      if (isList(args1) && isList(args2)) {
         list1 = toJavaUtilList(args1);
         list2 = toJavaUtilList(args2);
      } else if (isList(args1) && args2.type().isVariable()) {
         list1 = toJavaUtilList(args1);
         args2.unify(createListOfLength(list1.size()));
         list2 = toJavaUtilList(args2);
      } else if (isList(args2) && args1.type().isVariable()) {
         list2 = toJavaUtilList(args2);
         args1.unify(createListOfLength(list2.size()));
         list1 = toJavaUtilList(args1);
      } else {
         return false;
      }

      int listSize = list1.size();
      if (listSize != list2.size()) {
         return false;
      }

      final PredicateFactory pf = getPredicateFactory(getKnowledgeBase(), partiallyAppliedFunction, 2);
      for (int i = 0; i < listSize; i++) {
         if (!apply(pf, createArguments(partiallyAppliedFunction, list1.get(i), list2.get(i)))) {
            return false;
         }
      }
      return true;
   }

   private boolean isValidArguments(PTerm partiallyAppliedFunction, PTerm arg) {
      return isAtomOrStructure(partiallyAppliedFunction) && isList(arg);
   }
}
