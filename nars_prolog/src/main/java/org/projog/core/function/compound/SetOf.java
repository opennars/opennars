package org.projog.core.function.compound;

import static org.projog.core.term.TermComparator.TERM_COMPARATOR;

import java.util.List;

import org.projog.core.KnowledgeBase;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermComparator;

/* TEST
 z(r).
 z(t).
 z(y).

 x(a,b,c).
 x(q,X,e) :- z(X).
 x(1,2,3).
 x(w,b,c).
 x(d,b,c).
 x(a,b,c).

 %QUERY setof(X,x(X,Y,Z),L)
 %ANSWER
 % L=[a,d,w]
 % X=UNINSTANTIATED VARIABLE
 % Y=b
 % Z=c
 %ANSWER
 %ANSWER
 % L=[q]
 % X=UNINSTANTIATED VARIABLE
 % Y=r
 % Z=e
 %ANSWER
 %ANSWER
 % L=[q]
 % X=UNINSTANTIATED VARIABLE
 % Y=t
 % Z=e
 %ANSWER
 %ANSWER
 % L=[q]
 % X=UNINSTANTIATED VARIABLE
 % Y=y
 % Z=e
 %ANSWER
 %ANSWER
 % L=[1]
 % X=UNINSTANTIATED VARIABLE
 % Y=2
 % Z=3
 %ANSWER

 %FALSE setof(X,x(X,y,z),L)
 
 %QUERY setof(Y, (member(X,[6,3,7,2,5,4,3]), X<4, Y is X*X), L)
 %ANSWER
 % L=[9]
 % X=3
 % Y=UNINSTANTIATED VARIABLE
 %ANSWER
 %ANSWER
 % L=[4]
 % X=2
 % Y=UNINSTANTIATED VARIABLE
 %ANSWER
 */
/**
 * <code>setof(X,P,L)</code> - find all solutions that satisfy the goal.
 * <p>
 * <code>setof(X,P,L)</code> produces a list (<code>L</code>) of <code>X</code> for each possible solution of the goal
 * <code>P</code>. If <code>P</code> contains uninstantiated variables, other than <code>X</code>, it is possible that
 * <code>setof</code> can be successfully evaluated multiple times - for each possible values of the uninstantiated
 * variables. The elements in <code>L</code> will appear in sorted order and will not include duplicates. Fails if
 * <code>P</code> has no solutions.
 */
public final class SetOf extends AbstractCollectionOf {
   /** needed to create prototype actual instances can be created from */
   public SetOf() {
   }

   private SetOf(KnowledgeBase kb) {
      setKnowledgeBase(kb);
   }

   @Override
   public SetOf getPredicate(PTerm template, PTerm goal, PTerm bag) {
      return new SetOf(getKnowledgeBase());
   }

   /** "setof" excludes duplicates and orders elements using {@link TermComparator}. */
   @Override
   protected void add(List<PTerm> list, PTerm newTerm) {
      final int numberOfElements = list.size();
      for (int i = 0; i < numberOfElements; i++) {
         final PTerm next = list.get(i);
         final int comparison = TERM_COMPARATOR.compare(newTerm, next);
         if (comparison < 0) {
            // found correct position - so add
            list.add(i, newTerm);
            return;
         } else if (comparison == 0 && newTerm.strictEquals(next)) {
            // duplicate - so ignore
            return;
         }
      }
      list.add(newTerm);
   }
}