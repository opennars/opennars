package org.projog.core.udp.interpreter;

import org.projog.core.KB;
import org.projog.core.PredicateFactory;
import org.projog.core.function.bool.True;
import org.projog.core.function.flow.Cut;
import org.projog.core.term.PTerm;
import org.projog.core.term.PrologOperator;
import org.projog.core.udp.ClauseModel;

import java.util.HashSet;
import java.util.Set;

import static org.projog.core.KnowledgeBaseUtils.isConjunction;
import static org.projog.core.KnowledgeBaseUtils.isSingleAnswer;

/**
 * Constructs new {@link ClauseAction} instances.
 */
public final class ClauseActionFactory {
   /**
    * Returns a new {@link ClauseAction} based on the specified {@link ClauseModel}.
    */
   public static ClauseAction getClauseAction(KB kb, ClauseModel clauseModel) {
      PTerm consequent = clauseModel.getConsequent();
      PTerm antecedant = clauseModel.getAntecedant();
      if (antecedant.type().isVariable()) {
         return new SingleFunctionMultiResultClauseAction(kb, clauseModel);
      }
      PredicateFactory ef = kb.getPredicateFactory(antecedant);

      if (ef.getClass() == True.class) {
         return createClauseActionWithNoAntecedant(consequent);
      } else if (ef.getClass() == Cut.class) {
         return new CutClauseAction(kb, consequent.terms());
      } else if (isSingleAnswer(kb, antecedant)) {
         if (isConjunction(antecedant)) {
            return new MultiFunctionSingleResultClauseAction(kb, clauseModel);
         } else {
            return new SingleFunctionSingleResultClauseAction(kb, clauseModel);
         }
      } else {
         // NOTE: if it can give more than one result per call to evaluate, 
         // a conjunction is treated as a single function (not an array of many functions) 
         return new SingleFunctionMultiResultClauseAction(kb, clauseModel);
      }
   }

   private static ClauseAction createClauseActionWithNoAntecedant(PTerm consequent) {
      if (consequent.length() == 0) {
         return new AlwaysMatchedClauseAction(null);
      }

      // if all non-shared variables then always true
      // if all concrete terms (no variables) then reusable
      boolean hasVariables = false;
      boolean hasConcreteTerms = false;
      boolean hasSharedVariables = false;
      Set<PTerm> variables = new HashSet<>();
      for (PTerm t : consequent.terms()) {
         if (t.type() == PrologOperator.NAMED_VARIABLE) {
            hasVariables = true;
            if (!variables.add(t)) {
               hasSharedVariables = true;
            }
         } else {
            hasConcreteTerms = true;
            if (t.constant() == false) {
               hasVariables = true;
            }
         }
      }

      if (!hasSharedVariables && !hasConcreteTerms) {
         return new AlwaysMatchedClauseAction(consequent.terms());
      } else if (hasConcreteTerms && !hasVariables) {
         return new ImmutableArgumentsClauseAction(consequent.terms());
      } else {
         return new MutableArgumentsClauseAction(consequent.terms());
      }
   }
}