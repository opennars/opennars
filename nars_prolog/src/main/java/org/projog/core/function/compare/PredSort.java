package org.projog.core.function.compare;

import static org.projog.core.term.TermUtils.getAtomName;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.ListFactory;
import org.projog.core.term.ListUtils;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;

/* TEST
 %QUERY predsort(compare, [s,d,f,a,a,a,z], X)
 %ANSWER X=[a,a,a,d,f,s,z]
 
 %TRUE predsort(compare, [s,d,f,a,a,a,z], [a,a,a,d,f,s,z])
 %FALSE predsort(compare, [s,d,f,a,a,a,z], [s,d,f,a,a,a,z])

 %TRUE predsort(compare, [], [])
 */
/**
 * <code>predsort(X,Y,Z)</code> - sorts a list using the specifed predicate.
 * <p>
 * Sorts the list represented by <code>Y</code> using the predicate represented by <code>X</code> - and attempts to unify the result with <code>Z</code>.
 * The predicate represented by <code>X</code> must indicate whether the second argument is equal, less than or greater than the third argument -  
 * by unifying the first argument with an atom which has the value <code>=</code>, <code>&lt;</code> or <code>&gt;</code>. 
 * </p>
 * </
 */
public final class PredSort extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm predicateName, PTerm input, PTerm sorted) {
      List<PTerm> list = ListUtils.toJavaUtilList(input);
      if (list == null) {
         return false;
      }

      PredicateFactory pf = getPredicateFactory(predicateName);

      Collections.sort(list, new PredSortComparator(pf));

      return sorted.unify(ListFactory.createList(list));
   }

   private PredicateFactory getPredicateFactory(PTerm predicateName) {
      PredicateKey key = new PredicateKey(getAtomName(predicateName), 3);
      return getKB().getPredicateFactory(key);
   }

   private static final class PredSortComparator implements Comparator<PTerm> {
      private final PredicateFactory pf;

      private PredSortComparator(PredicateFactory pf) {
         this.pf = pf;
      }

      @Override
      public int compare(PTerm o1, PTerm o2) {
         PVar result = new PVar("PredSortResult");
         PTerm[] args = new PTerm[] {result, o1, o2};
         if (pf.getPredicate(args).evaluate(args)) {
            String delta = getAtomName(result);
            switch (delta) {
               case "<":
                  return -1;
               case ">":
                  return 1;
               case "=":
                  return 0;
               default:
                  throw new IllegalArgumentException(delta);
            }
         } else {
            throw new IllegalStateException();
         }
      }
   }
}
