package org.projog.core.function.kb;

import static org.projog.core.KnowledgeBaseUtils.getCalculatables;
import static org.projog.core.term.TermUtils.getAtomName;

import org.projog.core.Calculatables;
import org.projog.core.PredicateKey;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %QUERY X is sum(1, 1)
 %ERROR Cannot find calculatable: sum/2
 
 %TRUE pj_add_calculatable(sum/2, 'org.projog.core.function.math.Add')
 
 %QUERY X is sum(1, 1)
 %ANSWER X=2
 */
/**
 * <code>pj_add_calculatable(X,Y)</code> - defines a Java class as an arithmetic function.
 * <p>
 * <code>X</code> represents the name and arity of the predicate. <code>Y</code> represents the full class name of an
 * implementation of <code>org.projog.core.Calculatable</code>.
 */
public final class AddCalculatable extends AbstractSingletonPredicate {
   private Calculatables calculatables;

   @Override
   public void init() {
      calculatables = getCalculatables(getKB());
   }

   @Override
   public boolean evaluate(PTerm functionNameAndArity, PTerm javaClass) {
      PredicateKey key = PredicateKey.createFromNameAndArity(functionNameAndArity);
      String className = getAtomName(javaClass);
      calculatables.addCalculatable(key, className);
      return true;
   }
}
