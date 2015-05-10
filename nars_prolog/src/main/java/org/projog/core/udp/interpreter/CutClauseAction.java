package org.projog.core.udp.interpreter;

import java.util.Map;

import org.projog.core.CutException;
import org.projog.core.KnowledgeBase;
import org.projog.core.term.PTerm;
import org.projog.core.term.Variable;

/**
 * A clause whose body consists of a single cut ({@code !}) predicate.
 * <p>
 * e.g. {@code p(a,b,c) :- !.}
 * 
 * @see org.projog.core.function.flow.Cut
 */
public final class CutClauseAction extends AbstractMultiAnswerClauseAction {
   CutClauseAction(KnowledgeBase kb, PTerm[] consequentArgs) {
      super(kb, consequentArgs);
   }

   private CutClauseAction(CutClauseAction action) {
      super(action);
   }

   @Override
   protected boolean evaluateAntecedant(Map<Variable, Variable> sharedVariables) {
      return true;
   }

   @Override
   protected boolean reEvaluateAntecedant() {
      throw CutException.CUT_EXCEPTION;
   }

   @Override
   public CutClauseAction getFree() {
      return new CutClauseAction(this);
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return true;
   }
}