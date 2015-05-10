package org.projog.core.udp.interpreter;

import static org.projog.core.KnowledgeBaseUtils.toArrayOfConjunctions;

import java.util.Map;

import org.projog.core.KnowledgeBase;
import org.projog.core.PredicateFactory;
import org.projog.core.term.PTerm;
import org.projog.core.term.Variable;
import org.projog.core.udp.ClauseModel;

/**
 * A functions whose body is a conjunction consisting only of non-retryable predicates.
 * <p>
 * e.g. {@code p(A,B,C,D) :- A<B, B<C, C<D.}
 */
public final class MultiFunctionSingleResultClauseAction extends AbstractSingleAnswerClauseAction {
   private final PredicateFactory[] predicateFactories;
   private final PTerm[] originalTerms;

   MultiFunctionSingleResultClauseAction(KnowledgeBase kb, ClauseModel ci) {
      super(kb, ci.getConsequent().getArgs());
      originalTerms = toArrayOfConjunctions(ci.getAntecedant());
      predicateFactories = new PredicateFactory[originalTerms.length];
      for (int i = 0; i < originalTerms.length; i++) {
         predicateFactories[i] = kb.getPredicateFactory(originalTerms[i]);
      }
   }

   @Override
   protected boolean evaluateAntecedant(Map<Variable, Variable> sharedVariables) {
      for (int i = 0; i < originalTerms.length; i++) {
         PTerm t = originalTerms[i].copy(sharedVariables);
         if (!predicateFactories[i].getPredicate(t.getArgs()).evaluate(t.getArgs())) {
            return false;
         }
      }
      return true;
   }
}