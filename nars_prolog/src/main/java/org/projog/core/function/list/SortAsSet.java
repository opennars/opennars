package org.projog.core.function.list;

import static org.projog.core.term.ListFactory.createList;
import static org.projog.core.term.ListUtils.toSortedJavaUtilList;

import java.util.Iterator;
import java.util.List;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %QUERY sort([q,w,e,r,t,y], X)
 %ANSWER X = [e,q,r,t,w,y]
 
 %TRUE sort([q,w,e,r,t,y], [e,q,r,t,w,y])
 %FALSE sort([q,w,e,r,t,y], [q,w,e,r,t,y])
 %FALSE sort([q,w,e,r,t,y], [e,q,t,r,w,y])
 
 %QUERY sort([q,w,e,r,t,y], [A,B,C,D,E,F])
 %ANSWER
 % A=e
 % B=q
 % C=r
 % D=t
 % E=w
 % F=y
 %ANSWER

 %QUERY sort([], X)
 %ANSWER X=[]

 %QUERY sort([a], X)
 %ANSWER X=[a]

 %FALSE sort(a, X)
 %FALSE sort([a,b,c|T], X)
 
 %QUERY sort([h,e,l,l,o], X)
 %ANSWER X=[e,h,l,o]
 
 %FALSE sort([h,e,l,l,o], [e,h,l,l,o])
 */
/**
 * <code>sort(X,Y)</code> - sorts a list and removes duplicates.
 * <p>
 * Attempts to unify <code>Y</code> with a sorted version of the list represented by <code>X</code>, with duplicates removed.
 * </p>
 */
public final class SortAsSet extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm unsorted, PTerm sorted) {
      List<PTerm> elements = toSortedJavaUtilList(unsorted);
      if (elements == null) {
         return false;
      } else {
         removeDuplicates(elements);
         return sorted.unify(createList(elements));
      }
   }

   private void removeDuplicates(List<PTerm> elements) {
      Iterator<PTerm> itr = elements.iterator();
      PTerm previous = itr.hasNext() ? itr.next() : null;
      while (itr.hasNext()) {
         PTerm next = itr.next();
         if (previous.strictEquals(next)) {
            itr.remove();
         }
         previous = next;
      }
   }
}
