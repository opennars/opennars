package org.projog.core.function.list;

import static org.projog.core.term.ListFactory.createList;
import static org.projog.core.term.ListUtils.toJavaUtilList;

import java.util.Iterator;
import java.util.List;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %TRUE subtract([a,b,c,d,e,f], [a,s,d,f], [b,c,e])
 %TRUE subtract([a,b,a,a,d,c,d,e,f], [a,s,d,f], [b,c,e])
 %TRUE subtract([a,b,a,a,d,c,d,e,f], [], [a,b,a,a,d,c,d,e,f])
 %TRUE subtract([], [a,s,d,f], [])
 %TRUE subtract([], [], [])
 
 %QUERY subtract([a,a,a,a], [X], Z)
 %ANSWER
 % X=a
 % Z=[]
 %ANSWER
 
 %QUERY subtract([a,a,a,a,b], [X], Z)
 %ANSWER
 % X=a
 % Z=[b]
 %ANSWER
  
 %QUERY subtract([p(A),p(B),p(C)], [p(a)],Z)
 %ANSWER
 % A=a
 % B=a
 % C=a
 % Z=[]
 %ANSWER
 
 %QUERY subtract([p(a,B,c,e)], [p(A,b,C,e)], Z)
 %ANSWER
 % A=a
 % B=b
 % C=c
 % Z=[]
 %ANSWER
 
 %QUERY subtract([p(a,B,c,x)], [p(A,b,C,e)], Z)
 %ANSWER
 % A=UNINSTANTIATED VARIABLE
 % B=UNINSTANTIATED VARIABLE
 % C=UNINSTANTIATED VARIABLE
 % Z=[p(a, B, c, x)]
 %ANSWER
 
 %QUERY subtract([p(a,B), p(A,b)], [p(A,B)], Z)
 %ANSWER
 % A=a
 % B=b
 % Z=[]
 %ANSWER
 
 %FALSE subtract(X, [], [])
 %FALSE subtract([], X, [])
 %FALSE subtract(X, Y, [])
 */
/**
 * <code>subtract(X,Y,Z)</code> - removes elements from a list.
 * <p>
 * <code>subtract(X,Y,Z)</code> removes the elements in the list represented by <code>Y</code> 
 * from the list represented by <code>X</code> and attempts to unify the result with <code>Z</code>.
 */
public final class SubtractFromList extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(final PTerm original, final PTerm itemsToRemove, final PTerm result) {
      final List<PTerm> originalAsList = toJavaUtilList(original);
      final List<PTerm> itemsToRemoveAsList = toJavaUtilList(itemsToRemove);

      if (originalAsList == null || itemsToRemoveAsList == null) {
         return false;
      }

      final Iterator<PTerm> itr = originalAsList.iterator();
      while (itr.hasNext()) {
         final PTerm next = itr.next();
         if (shouldBeRemoved(next, itemsToRemoveAsList)) {
            itr.remove();
         }
      }

      return result.unify(createList(originalAsList));
   }

   private boolean shouldBeRemoved(final PTerm item, final List<PTerm> itemsToRemoveAsList) {
      for (PTerm itemToRemove : itemsToRemoveAsList) {
         if (isUnified(item, itemToRemove)) {
            return true;
         }
      }
      return false;
   }

   private boolean isUnified(PTerm item, PTerm itemToRemove) {
      item = item.get();
      itemToRemove = itemToRemove.get();

      if (item.unify(itemToRemove)) {
         return true;
      } else {
         item.backtrack();
         itemToRemove.backtrack();
         return false;
      }
   }
}
