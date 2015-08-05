package org.projog.core.function.list;

import org.projog.core.KB;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.projog.core.term.ListFactory.createList;
import static org.projog.core.term.ListUtils.toJavaUtilList;

/* TEST
 % Examples of when all three terms are lists:
 %TRUE append([a,b,c], [d,e,f], [a,b,c,d,e,f])
 %TRUE append([a], [b,c,d,e,f], [a,b,c,d,e,f])
 %TRUE append([a,b,c,d,e], [f], [a,b,c,d,e,f])
 %TRUE append([a,b,c,d,e,f], [], [a,b,c,d,e,f])
 %TRUE append([], [a,b,c,d,e,f], [a,b,c,d,e,f])
 %TRUE append([], [], [])
 %FALSE append([a,b], [d,e,f], [a,b,c,d,e,f])
 %FALSE append([a,b,c], [e,f], [a,b,c,d,e,f])
 %QUERY append([W,b,c], [d,Y,f], [a,X,c,d,e,Z])
 %ANSWER 
 % W=a
 % X=b
 % Y=e
 % Z=f
 %ANSWER
 
 % Examples of when first term is a variable:
 %QUERY append([a,b,c], X, [a,b,c,d,e,f])
 %ANSWER X=[d,e,f]
 %QUERY append([a,b,c,d,e], X, [a,b,c,d,e,f])
 %ANSWER X=[f]
 %QUERY append([a], X, [a,b,c,d,e,f])
 %ANSWER X=[b,c,d,e,f]
 %QUERY append([], X, [a,b,c,d,e,f])
 %ANSWER X=[a,b,c,d,e,f]
 %QUERY append([a,b,c,d,e,f], X, [a,b,c,d,e,f])
 %ANSWER X=[]
 
 % Examples of when second term is a variable:
 %QUERY append(X, [d,e,f], [a,b,c,d,e,f])
 %ANSWER X=[a,b,c]
 %QUERY append(X, [f], [a,b,c,d,e,f])
 %ANSWER X=[a,b,c,d,e]
 %QUERY append(X, [b,c,d,e,f], [a,b,c,d,e,f])
 %ANSWER X=[a]
 %QUERY append(X, [a,b,c,d,e,f], [a,b,c,d,e,f])
 %ANSWER X=[]
 %QUERY append(X, [], [a,b,c,d,e,f])
 %ANSWER X=[a,b,c,d,e,f]
 
 % Examples of when third term is a variable:
 %QUERY append([a,b,c], [d,e,f], X)
 %ANSWER X=[a,b,c,d,e,f]
 %QUERY append([a], [b,c,d,e,f], X)
 %ANSWER X=[a,b,c,d,e,f]
 %QUERY append([a,b,c,d,e], [f], X)
 %ANSWER X=[a,b,c,d,e,f]
 %QUERY append([a,b,c,d,e,f], [], X)
 %ANSWER X=[a,b,c,d,e,f]
 %QUERY append([], [a,b,c,d,e,f], X)
 %ANSWER X=[a,b,c,d,e,f]
 %QUERY append([], [], X)
 %ANSWER X=[]

 % Examples of when first and second terms are variables:
 %QUERY append(X, Y, [a,b,c,d,e,f])
 %ANSWER
 % X=[]
 % Y=[a,b,c,d,e,f]
 %ANSWER
 %ANSWER
 % X=[a]
 % Y=[b,c,d,e,f]
 %ANSWER
 %ANSWER
 % X=[a,b]
 % Y=[c,d,e,f]
 %ANSWER
 %ANSWER
 % X=[a,b,c]
 % Y=[d,e,f]
 %ANSWER
 %ANSWER
 % X=[a,b,c,d]
 % Y=[e,f]
 %ANSWER
 %ANSWER
 % X=[a,b,c,d,e]
 % Y=[f]
 %ANSWER
 %ANSWER
 % X=[a,b,c,d,e,f]
 % Y=[]
 %ANSWER
 %QUERY append(X, Y, [a])
 %ANSWER
 % X=[]
 % Y=[a]
 %ANSWER
 %ANSWER
 % X=[a]
 % Y=[]
 %ANSWER
 %QUERY append(X, Y, [])
 %ANSWER
 % X=[]
 % Y=[]
 %ANSWER

 % Examples when combination of term types cause failure:
 %QUERY append(X, Y, Z)
 %ERROR Expected list but got: NAMED_VARIABLE
 %FALSE append([], Y, Z)
 %FALSE append(X, [], Z)
 %FALSE append(a, b, c)
 %FALSE append(a, [], [])
 %FALSE append([], b, [])
 %FALSE append([], [], c)
 */
/**
 * <code>append(X,Y,Z)</code> - concatenates two lists.
 * <p>
 * The <code>append(X,Y,Z)</code> goal succeeds if the concatenation of lists <code>X</code> and <code>Y</code> matches
 * the list <code>Z</code>.
 * </p>
 */
public final class Append implements PredicateFactory {
   private final Singleton singleton = new Singleton();

   @Override
   public Predicate getPredicate(PTerm... args) {
      return getPredicate(args[0], args[1], args[2]);
   }

   public Predicate getPredicate(PTerm prefix, PTerm suffix, PTerm combined) {
      if (prefix.type().isVariable() && suffix.type().isVariable()) {
         List<PTerm> javaUtilList = toJavaUtilList(combined);
         if (javaUtilList == null) {
            throw new ProjogException("Expected list but got: " + combined.type());
         }
         return new Retryable(javaUtilList);
      } else {
         return singleton;
      }
   }

   @Override
   public void setKB(KB kb) {
      singleton.setKB(kb);
   }

   private static class Singleton extends AbstractSingletonPredicate {
      @Override
      public boolean evaluate(final PTerm prefix, final PTerm suffix, final PTerm concatenated) {
         final List<PTerm> prefixList = toJavaUtilList(prefix);
         final List<PTerm> suffixList = toJavaUtilList(suffix);

         if (prefixList != null && suffixList != null) {
            final List<PTerm> concatenatedList = new ArrayList<PTerm>();
            concatenatedList.addAll(prefixList);
            concatenatedList.addAll(suffixList);
            return concatenated.unify(createList(concatenatedList));
         }

         if (prefixList == null && suffixList == null) {
            return false;
         }

         final List<PTerm> concatenatedList = toJavaUtilList(concatenated);
         if (concatenatedList == null) {
            return false;
         }
         final int concatenatedLength = concatenatedList.size();

         final int splitIdx;
         if (prefixList != null) {
            splitIdx = prefixList.size();
         } else {
            splitIdx = concatenatedLength - suffixList.size();
         }

         return prefix.unify(createList(concatenatedList.subList(0, splitIdx))) && suffix.unify(createList(concatenatedList.subList(splitIdx, concatenatedLength)));
      }
   }

   private static class Retryable implements Predicate {
      final List<PTerm> combined;
      int ctr;

      @SuppressWarnings("unchecked")
      Retryable(List<PTerm> combined) {
         this.combined = combined == null ? Collections.EMPTY_LIST : combined;
      }

      @Override
      public boolean evaluate(PTerm... args) {
         return evaluate(args[0], args[1], args[2]);
      }

      private boolean evaluate(PTerm arg1, PTerm arg2, PTerm arg3) {
         while (couldReEvaluationSucceed()) {
            arg1.backtrack();
            arg2.backtrack();

            PTerm prefix = createList(combined.subList(0, ctr));
            PTerm suffix = createList(combined.subList(ctr, combined.size()));
            ctr++;

            return arg1.unify(prefix) && arg2.unify(suffix);
         }
         return false;
      }

      @Override
      public boolean isRetryable() {
         return true;
      }

      @Override
      public boolean couldReEvaluationSucceed() {
         return ctr <= combined.size();
      }
   }
}
