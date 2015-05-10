package org.projog.core.function.list;

import static org.projog.core.term.ListUtils.toJavaUtilList;
import static org.projog.core.term.TermUtils.toInt;

import java.util.Collections;
import java.util.List;

import org.projog.core.KnowledgeBase;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.PTerm;

/* TEST
 %TRUE nth0(0, [a,b,c], a)
 %TRUE nth1(1, [a,b,c], a)
 %TRUE nth0(1, [a,b,c], b)
 %TRUE nth1(2, [a,b,c], b)
 %TRUE nth0(2, [a,b,c], c)
 %TRUE nth1(3, [a,b,c], c)
 
 %FALSE nth0(-1, [a,b,c], a)
 %FALSE nth0(1, [a,b,c], a)
 %FALSE nth0(5, [a,b,c], a)

 %QUERY nth0(0, [a,b,c], X)
 %ANSWER X=a
 %QUERY nth0(1, [a,b,c], X)
 %ANSWER X=b
 %QUERY nth0(2, [a,b,c], X)
 %ANSWER X=c

 %FALSE nth0(-1, [a,b,c], X)
 %FALSE nth0(3, [a,b,c], X)

 %QUERY nth0(X, [h,e,l,l,o], e)
 %ANSWER X=1
 %NO
 %QUERY nth0(X, [h,e,l,l,o], l)
 %ANSWER X=2
 %ANSWER X=3
 %NO
 %FALSE nth0(X, [h,e,l,l,o], z)

 %QUERY nth0(X, [h,e,l,l,o], Y)
 %ANSWER 
 % X=0
 % Y=h
 %ANSWER
 %ANSWER 
 % X=1
 % Y=e
 %ANSWER
 %ANSWER 
 % X=2
 % Y=l
 %ANSWER
 %ANSWER 
 % X=3
 % Y=l
 %ANSWER
 %ANSWER 
 % X=4
 % Y=o
 %ANSWER

 %FALSE nth1(0, [a,b,c], a)
 %FALSE nth1(2, [a,b,c], a)
 %FALSE nth1(4, [a,b,c], a)

 %QUERY nth1(1, [a,b,c], X)
 %ANSWER X=a
 %QUERY nth1(2, [a,b,c], X)
 %ANSWER X=b
 %QUERY nth1(3, [a,b,c], X)
 %ANSWER X=c

 %FALSE nth1(-1, [a,b,c], X)
 %FALSE nth1(0, [a,b,c], X)
 %FALSE nth1(4, [a,b,c], X)

 %QUERY nth1(X, [h,e,l,l,o], e)
 %ANSWER X=2
 %NO
 %QUERY nth1(X, [h,e,l,l,o], l)
 %ANSWER X=3
 %ANSWER X=4
 %NO
 %FALSE nth1(X, [h,e,l,l,o], z)

 %QUERY nth1(X, [h,e,l,l,o], Y)
 %ANSWER 
 % X=1
 % Y=h
 %ANSWER
 %ANSWER 
 % X=2
 % Y=e
 %ANSWER
 %ANSWER 
 % X=3
 % Y=l
 %ANSWER
 %ANSWER 
 % X=4
 % Y=l
 %ANSWER
 %ANSWER 
 % X=5
 % Y=o
 %ANSWER

 % Note: "nth" is a synonym for "nth1".
 %TRUE nth(2, [a,b,c], b)
 */
/**
 * <code>nth0(X,Y,Z)</code> / <code>nth1(X,Y,Z)</code> - examines an element of a list.
 * <p>
 * Indexing starts at 0 when using <code>nth0</code>. Indexing starts at 1 when using <code>nth1</code>.
 * </p>
 */
public final class Nth implements PredicateFactory {
   public static Nth nth0() {
      return new Nth(0);
   }

   public static Nth nth1() {
      return new Nth(1);
   }

   private final Singleton singleton = new Singleton();
   private final int startingIdx;

   private Nth(int startingIdx) {
      this.startingIdx = startingIdx;
   }

   @Override
   public Predicate getPredicate(PTerm... args) {
      return getPredicate(args[0], args[1], args[2]);
   }

   public Predicate getPredicate(PTerm index, PTerm list, PTerm element) {
      if (index.type().isVariable()) {
         return new Retryable(toJavaUtilList(list));
      } else {
         return singleton;
      }
   }

   @Override
   public void setKnowledgeBase(KnowledgeBase kb) {
      singleton.setKnowledgeBase(kb);
   }

   private class Singleton extends AbstractSingletonPredicate {
      @Override
      protected boolean evaluate(PTerm index, PTerm list, PTerm element) {
         List<PTerm> l = toJavaUtilList(list);
         if (l == null) {
            return false;
         }

         int i = toInt(index);
         int idx = i - startingIdx;
         if (isValidIndex(l, idx)) {
            return element.unify(l.get(idx));
         } else {
            return false;
         }
      }

      private boolean isValidIndex(List<PTerm> l, int idx) {
         return idx > -1 && idx < l.size();
      }
   };

   private class Retryable implements Predicate {
      final List<PTerm> javaUtilList;
      int ctr;

      @SuppressWarnings("unchecked")
      Retryable(List<PTerm> javaUtilList) {
         this.javaUtilList = javaUtilList == null ? Collections.EMPTY_LIST : javaUtilList;
      }

      @Override
      public boolean evaluate(PTerm... args) {
         return evaluate(args[0], args[1], args[2]);
      }

      private boolean evaluate(PTerm index, PTerm list, PTerm element) {
         while (couldReEvaluationSucceed()) {
            backtrack(index, list, element);
            PTerm t = javaUtilList.get(ctr);
            IntegerNumber n = new IntegerNumber(ctr + startingIdx);
            ctr++;
            if (index.unify(n) && element.unify(t)) {
               return true;
            }
         }
         return false;
      }

      //TODO add to TermUtils (plus 1 and 2 args versions)
      private void backtrack(PTerm index, PTerm list, PTerm element) {
         index.backtrack();
         list.backtrack();
         element.backtrack();
      }

      @Override
      public boolean isRetryable() {
         return true;
      }

      @Override
      public boolean couldReEvaluationSucceed() {
         return ctr < javaUtilList.size();
      }
   };
}
