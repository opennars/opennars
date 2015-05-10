package org.projog.core.function.bool;

import org.projog.core.function.AbstractSingletonPredicate;

/* TEST
 %FALSE fail

 a(1).
 a(2).
 a(3).

 test :- a(X), a(Y), write(Y), write(' '), write(X), nl, fail.

 %QUERY test
 %OUTPUT
 % 1 1
 % 2 1
 % 3 1
 % 1 2
 % 2 2
 % 3 2
 % 1 3
 % 2 3
 % 3 3
 %
 %OUTPUT
 */
/**
 * <code>fail</code> - always fails.
 * <p>
 * The goal <code>fail</code> always fails.
 * </p>
 */
public final class Fail extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate() {
      return false;
   }
}