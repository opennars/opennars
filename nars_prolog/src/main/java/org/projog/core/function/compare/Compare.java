package org.projog.core.function.compare;

import static org.projog.core.term.TermComparator.TERM_COMPARATOR;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PAtom;
import org.projog.core.term.PTerm;

/* TEST
 %QUERY compare(X, a, z)
 %ANSWER X=<

 %QUERY compare(X, a, a)
 %ANSWER X==

 %QUERY compare(X, z, a)
 %ANSWER X=>

 %FALSE compare(<, z, a)

 %TRUE compare(>, z, a)

 % All floating point numbers are less than all integers
 %QUERY compare(X, 1.0, 1)
 %ANSWER X=<

 %QUERY compare(X, a, Y)
 %ANSWER 
 % X=>
 % Y=UNINSTANTIATED VARIABLE
 %ANSWER

 %FALSE compare(=, X, Y)
 
 %QUERY X=Y, compare(=, X, Y)
 %ANSWER 
 % X=UNINSTANTIATED VARIABLE
 % Y=UNINSTANTIATED VARIABLE
 %ANSWER
 */
/**
 * <code>compare(X,Y,Z)</code> - compares arguments.
 * <p>
 * Compares the second and third arguments.
 * <ul>
 * <li>If second is greater than third then attempts to unify first argument with <code>&gt;</code></li>
 * <li>If second is less than third then attempts to unify first argument with <code>&lt;</code></li>
 * <li>If second is equal to third then attempts to unify first argument with <code>=</code></li>
 * </ul>
 */
public final class Compare extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm result, PTerm t1, PTerm t2) {
      final int i = TERM_COMPARATOR.compare(t1, t2);
      final String symbol;
      if (i < 0) {
         symbol = "<";
      } else if (i > 0) {
         symbol = ">";
      } else {
         symbol = "=";
      }
      return result.unify(new PAtom(symbol));
   }
}
