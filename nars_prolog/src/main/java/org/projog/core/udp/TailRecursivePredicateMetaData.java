package org.projog.core.udp;

import static org.projog.core.KnowledgeBaseUtils.isSingleAnswer;
import static org.projog.core.KnowledgeBaseUtils.toArrayOfConjunctions;

import java.util.List;

import org.projog.core.KB;
import org.projog.core.term.PTerm;
import org.projog.core.term.PrologOperator;

/**
 * Defines the characteristics of a tail recursive user defined predicate.
 * <p>
 * Projog uses the following rules to determine if a user defined predicate is "tail recursive" (and therefore suitable
 * for <i>tail recursion optimisation</i> using a {@link TailRecursivePredicate}):
 * <ul>
 * <li>The user defined predicate must consist of exactly 2 rules.</li>
 * <li>It must be possible to detect, at the point that the user defined predicate is defined, that the antecedant of
 * the first rule will never generate multiple solutions per-query.</li>
 * <li>If the antecedant of the second rule is not a conjunction, it must be a call to itself (i.e. the user defined
 * predicate being defined) - this is what makes the predicate recursive.</li>
 * <li>If the antecedant of the second rule is a conjunction, the final element (i.e. the tail) of the conjunction must
 * be a call to itself (i.e. the user defined predicate being defined) - this is what makes the predicate recursive. It
 * must be possible to detect, at the point that the user defined predicate is defined, that all elements prior to the
 * final element of the conjunction will never generate multiple solutions per-query.</li>
 * </ul>
 * </p>
 * <p>
 * Examples of tail recursive predicates suitable for <i>tail recursion optimisation</i>:
 * 
 * <pre>
 * :- list([]).
 * list([X|Xs]) :- list(Xs).
 * </pre>
 * 
 * <pre>
 * r(N).
 * r(N) :- N > 1, N1 is N-1, r(N1).
 * </pre>
 * </p>
 * 
 * <pre>
 * writeAndRepeat(N) :- write(N), nl.
 * writeAndRepeat(N) :- N > 1, N1 is N-1, writeAndRepeat(N1).
 * </pre>
 * 
 * @see TailRecursivePredicate
 */
public final class TailRecursivePredicateMetaData {
   private final ClauseModel firstClause;
   private final ClauseModel secondClause;
   private final boolean isPotentialSingleResult;
   private final boolean[] isTailRecursiveArgument;
   private final boolean[] isSingleResultIfArgumentImmutable;

   /**
    * Returns a new {@code TailRecursivePredicateMetaData} representing the user defined predicate defined by the
    * specified clauses or {@code null} if the predicate is not tail recursive.
    * 
    * @param clauses the clauses that the user defined predicate consists of
    * @return a new {@code TailRecursivePredicateMetaData} or {@code null} if the predicate is not tail recursive
    */
   public static TailRecursivePredicateMetaData create(KB kb, List<ClauseModel> clauses) {
      if (isTailRecursive(kb, clauses)) {
         return new TailRecursivePredicateMetaData(clauses);
      } else {
         return null;
      }
   }

   private static boolean isTailRecursive(KB kb, List<ClauseModel> terms) {
      if (terms.size() != 2) {
         return false;
      }

      ClauseModel firstTerm = terms.get(0);
      if (!isSingleAnswer(kb, firstTerm.getAntecedant())) {
         return false;
      }

      ClauseModel secondTerm = terms.get(1);
      return isAntecedantRecursive(kb, secondTerm);
   }

   private static boolean isAntecedantRecursive(KB kb, ClauseModel secondTerm) {
      PTerm consequent = secondTerm.getConsequent();
      PTerm antecedant = secondTerm.getAntecedant();
      PTerm[] functions = toArrayOfConjunctions(antecedant);
      PTerm lastFunction = functions[functions.length - 1];
      if (lastFunction.type() == PrologOperator.STRUCTURE && lastFunction.getName().equals(consequent.getName()) && lastFunction.length() == consequent.length()) {
         for (int i = 0; i < functions.length - 1; i++) {
            if (!isSingleAnswer(kb, functions[i])) {
               return false;
            }
         }
         return true;
      } else {
         return false;
      }
   }

   private static boolean isTail(PTerm list, PTerm term) {
      if (list.type() == PrologOperator.LIST) {
         PTerm actualTail = list.term(1);
         return actualTail.strictEquals(term);
      } else {
         return false;
      }
   }

   /**
    * @see TailRecursivePredicateMetaData#create(KB, List)
    */
   private TailRecursivePredicateMetaData(List<ClauseModel> clauses) {
      this.firstClause = clauses.get(0);
      this.secondClause = clauses.get(1);

      int numberOfArguments = firstClause.getConsequent().length();

      this.isTailRecursiveArgument = new boolean[numberOfArguments];
      this.isSingleResultIfArgumentImmutable = new boolean[numberOfArguments];

      PTerm firstRuleConsequent = firstClause.getConsequent();
      PTerm secondRuleConsequent = secondClause.getConsequent();
      PTerm secondRuleAntecedantFinalFunction = getFinalFunction(secondClause.getAntecedant());
      boolean firstRuleConsequentHasEmptyListAsAnArgument = false;
      for (int i = 0; i < numberOfArguments; i++) {
         PTerm secondRuleConsequentArgument = secondRuleConsequent.term(i);
         PTerm secondRuleAntecedantArgument = secondRuleAntecedantFinalFunction.term(i);
         if (isTail(secondRuleConsequentArgument, secondRuleAntecedantArgument)) {
            isTailRecursiveArgument[i] = true;
            if (firstRuleConsequent.term(i).type() == PrologOperator.EMPTY_LIST) {
               isSingleResultIfArgumentImmutable[i] = true;
               firstRuleConsequentHasEmptyListAsAnArgument = true;
            }
         }
      }

      this.isPotentialSingleResult = firstRuleConsequentHasEmptyListAsAnArgument;
   }

   private PTerm getFinalFunction(PTerm t) {
      PTerm[] functions = toArrayOfConjunctions(t);
      return functions[functions.length - 1];
   }

   public ClauseModel getFirstClause() {
      return firstClause;
   }

   public ClauseModel getSecondClause() {
      return secondClause;
   }

   public boolean isPotentialSingleResult() {
      return isPotentialSingleResult;
   }

   public boolean isTailRecursiveArgument(int idx) {
      return isTailRecursiveArgument[idx];
   }

   public boolean isSingleResultIfArgumentImmutable(int idx) {
      return isSingleResultIfArgumentImmutable[idx];
   }
}