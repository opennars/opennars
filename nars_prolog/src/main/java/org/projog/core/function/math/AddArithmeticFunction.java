package org.projog.core.function.math;

import org.projog.core.*;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Numeric;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;
import org.projog.core.term.TermUtils;

import java.util.Arrays;

import static org.projog.core.KnowledgeBaseUtils.getCalculatables;

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
      calculatables = getCalculatables(getKB());
   }

   @Override
   public boolean evaluate(PTerm arg) {
      final PredicateKey key = PredicateKey.createFromNameAndArity(arg);
      calculatables.addCalculatable(key, createCalculatable(key));
      return true;
   }

   private ArithmeticFunction createCalculatable(final PredicateKey key) {
      return new ArithmeticFunction(getKB(), key);
   }

   private static class ArithmeticFunction implements Calculatable {
      final KB kb;
      final int numArgs;
      final PredicateKey key;

      ArithmeticFunction(KB kb, PredicateKey originalKey) {
         this.kb = kb;
         this.numArgs = originalKey.getNumArgs();
         this.key = new PredicateKey(originalKey.getName(), numArgs + 1);
      }

      @Override
      public Numeric calculate(PTerm... args) {
         final PredicateFactory pf = kb.getPredicateFactory(key);
         final PVar result = new PVar("result");
         final PTerm[] argsPlusResult = createArgumentsIncludingResult(args, result);

         if (pf.getPredicate(argsPlusResult).evaluate(argsPlusResult)) {
            return TermUtils.castToNumeric(result);
         } else {
            throw new ProjogException("Could not evaluate: " + key + " with arguments: " + Arrays.toString(args));
         }
      }

      private PTerm[] createArgumentsIncludingResult(PTerm[] args, final PVar result) {
         final PTerm[] argsPlusResult = new PTerm[numArgs + 1];
         for (int i = 0; i < numArgs; i++) {
            argsPlusResult[i] = args[i].get();
         }
         argsPlusResult[numArgs] = result;
         return argsPlusResult;
      }

      @Override
      public void setKnowledgeBase(KB kb) {
         // do nothing (KnowledgeBase set in constructor)
      }
   }
}
