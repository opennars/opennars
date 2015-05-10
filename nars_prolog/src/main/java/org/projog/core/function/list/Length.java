package org.projog.core.function.list;

import static org.projog.core.term.ListFactory.createListOfLength;
import static org.projog.core.term.ListUtils.toJavaUtilList;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;

/* TEST
 %QUERY length([],X)
 %ANSWER X=0
 %QUERY length([a],X)
 %ANSWER X=1
 %QUERY length([a,b],X)
 %ANSWER X=2
 %QUERY length([a,b,c],X)
 %ANSWER X=3
 
 %FALSE length([a,b|c],X)
 %FALSE length([a,b],1)
 %FALSE length([a,b],3)
 %FALSE length(abc,3)
 
 %QUERY length(X,0)
 %ANSWER X=[]
 
 %QUERY length(X,1)
 %ANSWER X=[E0]
 
 %QUERY length(X,3)
 %ANSWER X=[E0,E1,E2]
 */
/**
 * <code>length(X,Y)</code> - determines the length of a list.
 * <p>
 * The <code>length(X,Y)</code> goal succeeds if the number of elements in the list <code>X</code> matches the integer
 * value <code>Y</code>.
 * </p>
 */
public final class Length extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(final PTerm list, final PTerm expectedLength) {
      if (list.type().isVariable()) {
         final int length = TermUtils.toInt(expectedLength);
         return list.unify(createListOfLength(length));
      } else {
         return checkLength(list, expectedLength);
      }
   }

   private boolean checkLength(final PTerm list, final PTerm expectedLength) {
      final java.util.List<PTerm> javaList = toJavaUtilList(list);
      if (javaList != null) {
         final IntegerNumber actualLength = new IntegerNumber(javaList.size());
         return expectedLength.unify(actualLength);
      } else {
         return false;
      }
   }
}