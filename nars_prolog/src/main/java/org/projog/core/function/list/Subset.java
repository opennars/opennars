package org.projog.core.function.list;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

import static org.projog.core.term.ListUtils.isMember;
import static org.projog.core.term.ListUtils.toJavaUtilList;

/* TEST
 %TRUE subset([],[])
 %TRUE subset([],[a,b,c])
 %TRUE subset([a],[a,b,c])
 %TRUE subset([b,c],[a,b,c])
 %TRUE subset([a,b,c],[a,b,c])
 %TRUE subset([c,a,b],[a,b,c])
 %TRUE subset([c,a,c,b,b,c],[b,a,b,a,c])

 %FALSE subset([a,b,c,d],[a,b,c])
 %FALSE subset([a,b,c],[])
 */
/**
 * <code>subset(X,Y)</code> - checks if a set is a subset.
 * <p>
 * True if each of the elements in the list represented by <code>X</code> can be unified with elements in the list
 * represented by <code>Y</code>.
 * </p>
 */
public final class Subset extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm subsetTerm, PTerm set) {
      for (PTerm element : toJavaUtilList(subsetTerm)) {
         if (!isMember(element, set)) {
            return false;
         }
      }
      return true;
   }
}
