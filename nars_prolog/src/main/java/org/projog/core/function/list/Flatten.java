package org.projog.core.function.list;

import java.util.ArrayList;
import java.util.List;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.EmptyList;
import org.projog.core.term.ListFactory;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermType;

/* TEST
 %QUERY flatten([a,[[b]],[c]], X)
 %ANSWER X=[a,b,c]
 
 %QUERY flatten([a,b,c], X)
 %ANSWER X=[a,b,c]

 %QUERY flatten([[[[a]]],[],[],[]], X)
 %ANSWER X=[a]

 %QUERY flatten([a], X)
 %ANSWER X=[a]
 
 %QUERY flatten(a, X)
 %ANSWER X=[a]

 %QUERY flatten([[[[]]],[],[],[]], X)
 %ANSWER X=[]

 %QUERY flatten([], X)
 %ANSWER X=[]
 
 %QUERY flatten([a|b], X)
 %ANSWER X=[a,b]

 %QUERY flatten([a|[]], X)
 %ANSWER X=[a]
 
 %QUERY flatten([[a|b],[c,d|e],[f|[]],g|h], X)
 %ANSWER X=[a,b,c,d,e,f,g,h]
 
 %QUERY flatten([p([[a]]),[[[p(p(x))]],[p([a,b,c])]]], X)
 %ANSWER X=[p([[a]]),p(p(x)),p([a,b,c])]
 
 %FALSE flatten([a,b,c], [c,b,a])
 %FALSE flatten([a,b,c], [a,[b],c])
 */
/**
 * <code>flatten(X,Y)</code> - flattens a nested list.
 * <p>
 * Flattens the nested list represented by <code>X</code> and attempts to unify it with <code>Y</code>.
 */
public final class Flatten extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(final PTerm original, final PTerm expected) {
      final PTerm flattenedVersion;
      switch (original.type()) {
         case LIST:
            flattenedVersion = ListFactory.createList(flattenList(original));
            break;
         case EMPTY_LIST:
            flattenedVersion = original;
            break;
         default:
            flattenedVersion = ListFactory.createList(original, EmptyList.EMPTY_LIST);
      }
      return expected.unify(flattenedVersion);
   }

   private List<PTerm> flattenList(final PTerm input) {
      List<PTerm> result = new ArrayList<PTerm>();
      PTerm next = input;
      while (next.type() == TermType.LIST) {
         PTerm head = next.arg(0);
         if (head.type() == TermType.LIST) {
            result.addAll(flattenList(head));
         } else if (head.type() != TermType.EMPTY_LIST) {
            result.add(head);
         }

         next = next.arg(1);
      }
      if (next.type() != TermType.EMPTY_LIST) {
         result.add(next);
      }
      return result;
   }
}
