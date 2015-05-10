package org.projog.core.udp.compiler;

import static org.projog.core.KnowledgeBaseUtils.toArrayOfConjunctions;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.projog.core.KnowledgeBase;
import org.projog.core.PredicateFactory;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.function.flow.Cut;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermType;
import org.projog.core.term.TermUtils;
import org.projog.core.term.Variable;
import org.projog.core.udp.ClauseModel;
import org.projog.core.udp.StaticUserDefinedPredicateFactory;

/**
 * Defines the characteristics of a clause belonging to a user defined predicate.
 * <p>
 * Instances of this class maintain state of a clause during the process of it being converted from Prolog syntax to
 * Java source code at runtime in order for it to be compiled to bytecode.
 */
final class ClauseMetaData {
   // TODO consider ways to improve this class through refactoring
   // In common with some other classes in org.projog.core.udp.compiler,
   // this class is large and it's intentions not always immediately obvious.
   // CompiledPredicateSourceGeneratorTest (which checks actual content of generated source files)
   // and the system tests (which check actual behaviour) should give confidence when refactoring. 

   private final int clauseIndex;
   private final PTerm term;
   private final PTerm consequent;
   private final PTerm[] conjunctions;
   private final Set<Variable>[] variablesInConjunction;
   private final PredicateFactory[] predicateFactories;
   private final boolean[] isConjunctionMulipleResult;
   private final boolean isSingleResult;
   private final boolean containsCut;
   private final int indexOfFirstMulipleResultConjuction;
   private final int indexOfLastMulipleResultConjuction;
   private final int numberOfMulipleResultConjuctions;
   /**
    * Stores IDs of variables used since last backtrack point
    * <p>
    * Uses LinkedHashSet to ensure a predictable order (just to make unit tests easier).
    */
   private final Set<Variable> variablesToBackTrack = new LinkedHashSet<>();
   private final Set<Variable> ignorableVariables;
   private int conjunctionIndex = -1;
   private int lastBacktrackPoint;
   private int retryablePredicateCtr;
   private int memberCompiledPredicatesCtr;
   private int termVariableCtr;
   private int numericCtr;

   @SuppressWarnings({"unchecked", "rawtypes"})
   ClauseMetaData(KnowledgeBase kb, int clauseIndex, ClauseModel clauseModel, boolean isTailRecursive) {
      this.clauseIndex = clauseIndex;
      this.term = clauseModel.getOriginal();
      this.consequent = clauseModel.getConsequent();
      PTerm antecedant = clauseModel.getAntecedant();
      this.conjunctions = toArrayOfConjunctions(antecedant);
      this.predicateFactories = new PredicateFactory[conjunctions.length];
      this.variablesInConjunction = new Set[conjunctions.length];
      this.isConjunctionMulipleResult = new boolean[conjunctions.length];
      boolean isSingleResult = true;
      boolean containsCut = false;
      int indexOfFirstMulipleResultConjuction = 0;
      int indexOfLastMulipleResultConjuction = 0;
      int numberOfMulipleResultConjuctions = 0;
      String consequentName = consequent.getName();
      for (int i = 0; i < conjunctions.length; i++) {
         PTerm c = conjunctions[i];
         variablesInConjunction[i] = TermUtils.getAllVariablesInTerm(c);
         boolean isRetryable;
         if (consequentName.equals(c.getName()) && c.args() == consequent.args()) {
            if (isTailRecursive && i == conjunctions.length - 1) {
               isRetryable = false;
            } else {
               isRetryable = true;
            }
         } else {
            predicateFactories[i] = getPredicateFactory(kb, c);
            if (predicateFactories[i] == null) {
               throw new RuntimeException();
            } else if (predicateFactories[i].getClass() == Cut.class) {
               containsCut = true;
               isRetryable = false;
            } else {
               isRetryable = isRetryable(predicateFactories[i], c.getArgs());
            }
         }
         if (isRetryable) {
            if (isSingleResult) {
               indexOfFirstMulipleResultConjuction = i;
               isSingleResult = false;
            }
            indexOfLastMulipleResultConjuction = i;
            numberOfMulipleResultConjuctions++;
            isConjunctionMulipleResult[i] = true;
         }
      }
      this.isSingleResult = isSingleResult;
      this.containsCut = containsCut;
      this.indexOfFirstMulipleResultConjuction = indexOfFirstMulipleResultConjuction;
      this.indexOfLastMulipleResultConjuction = indexOfLastMulipleResultConjuction;
      this.numberOfMulipleResultConjuctions = numberOfMulipleResultConjuctions;

      this.ignorableVariables = getConsequentVariablesThatCanBeIgnored();
   }

   private static boolean isRetryable(PredicateFactory ef, PTerm[] args) {
      if (ef instanceof AbstractRetryablePredicate) {
         return true;
      }
      if (ef instanceof AbstractSingletonPredicate) {
         return false;
      }
      if (ef instanceof CompiledPredicate) {
         return ef.getPredicate(args).isRetryable();
      }
      // default to it being retryable
      return true;
   }

   private PredicateFactory getPredicateFactory(KnowledgeBase kb, PTerm c) {
      PredicateFactory ef = kb.getPredicateFactory(c);
      if (ef instanceof StaticUserDefinedPredicateFactory) {
         return ((StaticUserDefinedPredicateFactory) ef).getActualPredicateFactory();
      } else {
         return ef;
      }
   }

   private Set<Variable> getConsequentVariablesThatCanBeIgnored() {
      Set<Variable> declaredOnce = new HashSet<>();
      getSingletonVariables(consequent, declaredOnce, new HashSet<Variable>());

      Set<Variable> antecedantVariables = getAntecedantVariables();

      Set<Variable> ignorableVariables = new HashSet<>();
      for (Variable v : declaredOnce) {
         if (antecedantVariables.contains(v) == false) {
            ignorableVariables.add(v);
         }
      }
      return ignorableVariables;
   }

   private void getSingletonVariables(PTerm argument, Set<Variable> declaredOnce, Set<Variable> declaredMany) {
      if (argument.type() == TermType.NAMED_VARIABLE) {
         Variable v = (Variable) argument;
         if (declaredMany.contains(v) == false) {
            if (declaredOnce.add(v) == false) {
               declaredOnce.remove(v);
               declaredMany.add(v);
            }
         }
      } else {
         for (int i = 0; i < argument.args(); i++) {
            getSingletonVariables(argument.arg(i), declaredOnce, declaredMany);
         }
      }
   }

   private Set<Variable> getAntecedantVariables() {
      final Set<Variable> antecedantVariables = new HashSet<>();
      for (Set<Variable> v : variablesInConjunction) {
         antecedantVariables.addAll(v);
      }
      return antecedantVariables;
   }

   boolean isInRetryMethod() {
      return isSingleResult() == false && conjunctionIndex >= getIndexOfFirstMulipleResultConjuction();
   }

   int getClauseIndex() {
      return clauseIndex;
   }

   PTerm getTerm() {
      return term;
   }

   PTerm getConsequent() {
      return consequent;
   }

   boolean isConjunctionMulipleResult(int i) {
      return isConjunctionMulipleResult[i];
   }

   boolean containsCut() {
      return containsCut;
   }

   boolean isSingleResult() {
      return isSingleResult;
   }

   int getIndexOfFirstMulipleResultConjuction() {
      return indexOfFirstMulipleResultConjuction;
   }

   int getNumberOfMulipleResultConjuctions() {
      return numberOfMulipleResultConjuctions;
   }

   PTerm getConjunction(int idx) {
      return conjunctions[idx];
   }

   PTerm getCurrentFunction() {
      return getConjunction(conjunctionIndex);
   }

   boolean isCurrentFunctionMulipleResult() {
      return isConjunctionMulipleResult(conjunctionIndex);
   }

   Set<Variable> getVariablesInConjunction(int idx) {
      return variablesInConjunction[idx];
   }

   Set<Variable> getVariablesInCurrentFunction() {
      return getVariablesInConjunction(conjunctionIndex);
   }

   PredicateFactory getPredicateFactory(int i) {
      return predicateFactories[i];
   }

   PredicateFactory getCurrentPredicateFactory() {
      return getPredicateFactory(conjunctionIndex);
   }

   boolean isFirstMutlipleResultFunctionInConjunction() {
      return indexOfFirstMulipleResultConjuction == conjunctionIndex;
   }

   int getConjunctionCount() {
      return conjunctions.length;
   }

   boolean isLastCutAfterLastBacktrackPoint() {
      if (containsCut() == false) {
         return false;
      }

      boolean b = false;
      for (int i = conjunctionIndex - 1; i > -1; i--) {
         if (isCut(i)) {
            return true;
         }
         if (isConjunctionMulipleResult[i]) {
            if (b) {
               return false;
            } else {
               b = true;
            }
         }
      }
      return false;
   }

   private boolean isCut(int idx) {
      return predicateFactories[idx] != null && predicateFactories[idx].getClass() == Cut.class;
   }

   boolean isAfterLastMulipleResultConjuction() {
      return isSingleResult == false && indexOfLastMulipleResultConjuction < conjunctionIndex;
   }

   int getLastBacktrackPoint() {
      return lastBacktrackPoint;
   }

   boolean isIgnorableVariable(PTerm v) {
      return ignorableVariables.contains(v);
   }

   int getConjunctionIndex() {
      return conjunctionIndex;
   }

   void setConjunctionIndex(int conjunctionIndex) {
      this.conjunctionIndex = conjunctionIndex;
   }

   void setLastBacktrackPoint(int lastBacktrackPoint) {
      this.lastBacktrackPoint = lastBacktrackPoint;
   }

   int getNextRetryablePredicateIndex() {
      return retryablePredicateCtr++;
   }

   int getNextMemberCompiledPredicatesIndex() {
      return memberCompiledPredicatesCtr++;
   }

   int getNextNumericIndex() {
      return numericCtr++;
   }

   int getNextTermVariableIndex() {
      return termVariableCtr++;
   }

   void clearVariablesToBackTrack() {
      variablesToBackTrack.clear();
   }

   void addVariablesToBackTrack(Set<Variable> variables) {
      variablesToBackTrack.addAll(variables);
   }

   boolean isNothingToBackTrack() {
      return variablesToBackTrack.isEmpty();
   }

   Set<Variable> getVariablesToBackTrack() {
      return variablesToBackTrack;
   }
}