package org.projog.core.function.list;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

import java.util.Iterator;
import java.util.List;

import static org.projog.core.term.ListFactory.createList;
import static org.projog.core.term.ListUtils.toJavaUtilList;

/* TEST
 %QUERY delete([a,b,c],a,X)
 %ANSWER X=[b,c]
 %QUERY delete([a,b,c],b,X)
 %ANSWER X=[a,c]
 %QUERY delete([a,b,c],c,X)
 %ANSWER X=[a,b]
 %QUERY delete([a,b,c],z,X)
 %ANSWER X=[a,b,c]
 
 %QUERY delete([a,b,X],a,[Y,c])
 %ANSWER 
 % X=c
 % Y=b
 %ANSWER

 %QUERY delete([a,b,c],Y,X)
 %ANSWER 
 % X=[a,b,c]
 % Y=UNINSTANTIATED VARIABLE
 %ANSWER
 %QUERY delete([a,Y,c],b,X)
 %ANSWER 
 % X=[a,Y,c]
 % Y=UNINSTANTIATED VARIABLE
 %ANSWER
 %QUERY delete([a,Y,_],_,X)
 %ANSWER 
 % X=[a,Y,_]
 % Y=UNINSTANTIATED VARIABLE
 %ANSWER
 %QUERY W=Y,delete([a,Y,_],W,X)
 %ANSWER 
 % X=[a,_]
 % W=UNINSTANTIATED VARIABLE
 % Y=UNINSTANTIATED VARIABLE
 %ANSWER

 %QUERY delete([],a,X)
 %ANSWER X=[]
 */
/**
 * <code>delete(X,Y,Z)</code> - remove all occurrences of a term from a list.
 * <p>
 * Removes all occurrences of the term <code>Y</code> in the list represented by <code>X</code> and attempts to unify
 * the result with <code>Z</code>. Strict term equality is used to identify occurrences.
 */
public final class Delete extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm input, PTerm element, PTerm output) {
      List<PTerm> javaList = toJavaUtilList(input);
      if (javaList == null) {
         return false;
      }

      Iterator<PTerm> itr = javaList.iterator();
      while (itr.hasNext()) {
         PTerm next = itr.next();
         if (element.strictEquals(next)) {
            itr.remove();
         }
      }

      return output.unify(createList(javaList));
   }
}
