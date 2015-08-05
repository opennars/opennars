package org.projog.core.function.list;

import org.projog.core.ProjogException;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.ListFactory;
import org.projog.core.term.ListUtils;
import org.projog.core.term.PTerm;

import java.util.ArrayList;
import java.util.List;

/* TEST
 %QUERY select(X,[h,e,l,l,o],Z)
 %ANSWER
 % X=h
 % Z=[e,l,l,o]
 %ANSWER
 %ANSWER
 % X=e
 % Z=[h,l,l,o]
 %ANSWER
 %ANSWER
 % X=l
 % Z=[h,e,l,o]
 %ANSWER
 %ANSWER
 % X=l
 % Z=[h,e,l,o]
 %ANSWER
 %ANSWER
 % X=o
 % Z=[h,e,l,l]
 %ANSWER
 
 %QUERY select(l,[h,e,l,l,o],Z)
 %ANSWER Z=[h,e,l,o]
 %ANSWER Z=[h,e,l,o]
 %NO

 %QUERY select(l,[h,e,l,l,o],[h,e,l,o])
 %ANSWER/
 %ANSWER/
 %NO

 %QUERY select(p(a,B),[p(X,q), p(a,X)],Z)
 %ANSWER
 % B=q
 % X=a
 % Z=[p(a, a)]
 %ANSWER
 %ANSWER
 % B=UNINSTANTIATED VARIABLE
 % X=UNINSTANTIATED VARIABLE  
 % Z=[p(X, q)]
 %ANSWER
 */
/**
 * <code>select(X,Y,Z)</code> - removes an element from a list.
 * <p>
 * Attempts to unify <code>Z</code> with the result of removing an occurrence of <code>X</code> from the list represented by <code>Y</code>. 
 * An attempt is made to retry the goal during backtracking. 
 * </p>
 */
public final class Select extends AbstractRetryablePredicate {
   private final List<PTerm> list;
   private int ctr;

   public Select() {
      this(null);
   }

   private Select(List<PTerm> list) {
      this.list = list;
   }

   @Override
   public Select getPredicate(PTerm element, PTerm inputList, PTerm outputList) {
      List<PTerm> list = ListUtils.toJavaUtilList(inputList);
      if (list == null) {
         throw new ProjogException("Expected list but got: " + inputList.type());
      }
      return new Select(list);
   }

   @Override
   public boolean evaluate(PTerm element, PTerm inputList, PTerm outputList) {
      while (couldReEvaluationSucceed()) {
         if (retrying()) {
            element.backtrack();
            inputList.backtrack();
            outputList.backtrack();
         }

         PTerm listElement = list.get(ctr);
         boolean unified = element.unify(listElement) && outputList.unify(exclude(ctr));
         ctr++;
         if (unified) {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return ctr < list.size();
   }

   private boolean retrying() {
      return ctr > 0;
   }

   /**  Create a a new {@code org.projog.core.term.List} based on {@code list} but excluding the element at index {@code indexOfElementToExclude}. */
   private PTerm exclude(int indexOfElementToExclude) {
      final int size = list.size();
      final List<PTerm> result = new ArrayList<PTerm>(size - 1);
      for (int i = 0; i < size; i++) {
         if (i != ctr) {
            result.add(list.get(i));
         }
      }
      return ListFactory.createList(result);
   }
}
