package org.projog.core.function.compound;

import org.projog.core.KnowledgeBaseUtils;
import org.projog.core.Predicate;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %FALSE \+ true
 %TRUE \+ fail
 
 % Note: "not" is a synonym for "\+".
 %FALSE not(true)
 %TRUE not(fail)
 */
/**
 * <code>\+ X</code> - "not".
 * <p>
 * The <code>\+ X</code> goal succeeds if an attempt to satisfy the goal represented by the term <code>X</code> fails.
 * The <code>\+ X</code> goal fails if an attempt to satisfy the goal represented by the term <code>X</code> succeeds.
 * </p>
 */
public final class Not extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm t) {
      Predicate e = KnowledgeBaseUtils.getPredicate(getKB(), t);
      return !e.evaluate(t.terms());
   }
}