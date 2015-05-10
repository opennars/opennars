package org.projog.core.function.math;

import static org.projog.core.KnowledgeBaseUtils.getCalculatables;

import java.util.Arrays;

import org.projog.core.Calculatable;
import org.projog.core.Calculatables;
import org.projog.core.KnowledgeBase;
import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Numeric;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;
import org.projog.core.term.Variable;

/* TEST
 squared(X,Y) :- Y is X * X.
 
 %QUERY squared(3,X)
 %ANSWER X=9
 
 %QUERY X is squared(3)
 %ERROR Cannot find calculatable: squared/1
 
 %TRUE arithmetic_function(squared/1)
 
 %QUERY X is squared(3)
 %ANSWER X=9
 */
/**
 * <code>arithmetic_function(X)</code> - defines a predicate as an arithmetic function.
 * <p>
 * Allows the predicate defined by <code>X</code> to be used as an arithmetic function.
 */
public final class AddArithmeticFunction extends AbstractSingletonPredicate {
   private Calculatables calculatables;

   @Override
   public void init() {
      calculatables = getCalculatables(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(PTerm arg) {
      final PredicateKey key = PredicateKey.createFromNameAndArity(arg);
      calculatables.addCalculatable(key, createCalculatable(key));
      return true;
   }

   private ArithmeticFunction createCalculatable(final PredicateKey key) {
      return new ArithmeticFunction(getKnowledgeBase(), key);
   }

   private static class ArithmeticFunction implements Calculatable {
      final KnowledgeBase kb;
      final int numArgs;
      final PredicateKey key;

      ArithmeticFunction(KnowledgeBase kb, PredicateKey originalKey) {
         this.kb = kb;
         this.numArgs = originalKey.getNumArgs();
         this.key = new PredicateKey(originalKey.getName(), numArgs + 1);
      }

      @Override
      public Numeric calculate(PTerm... args) {
         final PredicateFactory pf = kb.getPredicateFactory(key);
         final Variable result = new Variable("result");
         final PTerm[] argsPlusResult = createArgumentsIncludingResult(args, result);

         if (pf.getPredicate(argsPlusResult).evaluate(argsPlusResult)) {
            return TermUtils.castToNumeric(result);
         } else {
            throw new ProjogException("Could not evaluate: " + key + " with arguments: " + Arrays.toString(args));
         }
      }

      private PTerm[] createArgumentsIncludingResult(PTerm[] args, final Variable result) {
         final PTerm[] argsPlusResult = new PTerm[numArgs + 1];
         for (int i = 0; i < numArgs; i++) {
            argsPlusResult[i] = args[i].get();
         }
         argsPlusResult[numArgs] = result;
         return argsPlusResult;
      }

      @Override
      public void setKnowledgeBase(KnowledgeBase kb) {
         // do nothing (KnowledgeBase set in constructor)
      }
   }
}
