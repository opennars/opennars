package org.projog.core.function.construct;

import static org.projog.core.term.TermUtils.toInt;

import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %QUERY arg(2, a(b,c(d)), X)
 %ANSWER X=c(d)

 %QUERY arg(1, a+(b+c), X )
 %ANSWER X=a

 %FALSE arg(1, a+(b+c), b)

 %QUERY arg(2, [a,b,c], X)
 %ANSWER X=[b,c]
 
 %QUERY arg(3, [a,b,c], X)
 %ERROR Cannot get argument at position: 3 from: .(a, .(b, .(c, [])))
 */
/**
 * <code>arg(N,T,A)</code> - allows access to an argument of a structure.
 * <p>
 * <code>arg(N,T,A)</code> provides a mechanism for accessing a specific argument of a structure.
 * <code>arg(N,T,A)</code> succeeds if the <code>N</code>th argument of the structure <code>T</code> is, or can be
 * assigned to, <code>A</code>.
 * </p>
 */
public final class Arg extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm arg1, PTerm arg2, PTerm arg3) {
      int argIdx = toInt(arg1);
      if (arg2.length() < argIdx) {
         throw new ProjogException("Cannot get argument at position: " + argIdx + " from: " + arg2);
      }
      PTerm t = arg2.term(argIdx - 1);
      return arg3.unify(t);
   }
}