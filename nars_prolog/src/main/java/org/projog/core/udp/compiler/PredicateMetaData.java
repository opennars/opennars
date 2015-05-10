package org.projog.core.udp.compiler;

import java.util.List;

import org.projog.core.KnowledgeBase;
import org.projog.core.PredicateKey;
import org.projog.core.udp.ClauseModel;
import org.projog.core.udp.TailRecursivePredicateMetaData;

/**
 * Defines the characteristics of a user defined predicate.
 */
final class PredicateMetaData {
   private final PredicateKey key;
   private final ClauseMetaData[] clauses;
   private final boolean isSingleResultPredicate;
   private final boolean isTailRecursive;
   private final TailRecursivePredicateMetaData recursiveFunctionMetaData;

   private final int numberArguments;

   PredicateMetaData(KnowledgeBase kb, List<ClauseModel> implications) {
      this.recursiveFunctionMetaData = TailRecursivePredicateMetaData.create(kb, implications);
      this.isTailRecursive = recursiveFunctionMetaData != null;
      this.clauses = new ClauseMetaData[implications.size()];
      for (int i = 0; i < clauses.length; i++) {
         ClauseModel clauseModel = implications.get(i);
         clauses[i] = new ClauseMetaData(kb, i, clauseModel, isTailRecursive);
      }
      this.key = PredicateKey.createForTerm(clauses[0].getConsequent());
      this.numberArguments = clauses[0].getConsequent().args();
      this.isSingleResultPredicate = (clauses.length == 1 && clauses[0].isSingleResult());
   }

   PredicateKey getPredicateKey() {
      return key;
   }

   int getNumberArguments() {
      return numberArguments;
   }

   ClauseMetaData[] getClauses() {
      return clauses;
   }

   ClauseMetaData getClause(int idx) {
      return clauses[idx];
   }

   boolean isSingleResultPredicate() {
      return isSingleResultPredicate;
   }

   boolean isTailRecursive() {
      return isTailRecursive;
   }

   boolean isTailRecursiveArgument(int idx) {
      return isTailRecursive && recursiveFunctionMetaData.isTailRecursiveArgument(idx);
   }

   boolean isPossibleSingleResultRecursiveFunction() {
      return isTailRecursive && recursiveFunctionMetaData.isPotentialSingleResult();
   }

   boolean isSingleResultIfArgumentImmutable(int idx) {
      return isTailRecursive && recursiveFunctionMetaData.isSingleResultIfArgumentImmutable(idx);
   }

   boolean isCutVariableRequired() {
      for (ClauseMetaData c : clauses) {
         if (c.containsCut()) {
            return true;
         }
      }
      return false;
   }

   boolean isMultipleAnswersClause() {
      for (ClauseMetaData c : clauses) {
         if (c.isSingleResult() == false) {
            return true;
         }
      }
      return false;
   }
}