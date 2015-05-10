package org.projog.core.function.time;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.PTerm;

/* TEST
 validate_get_time :- get_time(X), X>1000000000000, get_time(Y), Y>=X.

 %TRUE validate_get_time
 */
/**
 * <code>get_time(X)</code> - gets the current system time.
 * <p>
 * Attempts to unify <code>X</code> with an integer term representing the value returned from <code>java.lang.System.currentTimeMillis()</code>. 
 * </p>
 */
public final class GetTime extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(PTerm t) {
      IntegerNumber currentTime = new IntegerNumber(System.currentTimeMillis());
      return t.unify(currentTime);
   }
}
