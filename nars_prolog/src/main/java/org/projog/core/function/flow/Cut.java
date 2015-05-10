package org.projog.core.function.flow;

import org.projog.core.CutException;
import org.projog.core.function.AbstractRetryablePredicate;

/* TEST
 %QUERY repeat, !
 %ANSWER/
 %NO

 print_first_sentence(X) :- 
    atom_chars(X, Chars), member(Next, Chars), write(Next), Next=='.', !.
 
 %QUERY print_first_sentence('word1 word2 word3. word4 word5 word6.')
 %OUTPUT word1 word2 word3.
 %ANSWER/
 %NO
 
 a(x, Y) :- Y = 1, !.
 a(X, Y) :- Y = 2.

 %QUERY a(x, Y)
 %ANSWER Y = 1
 %NO

 %QUERY a(y, Y)
 %ANSWER Y = 2

 %QUERY a(z, Y)
 %ANSWER Y = 2
 */
/**
 * <code>!</code> - the "cut".
 * <p>
 * The "cut", represented as a <code>!</code>, is a special mechanism which affects how prolog backtracks.
 * </p>
 */
public final class Cut extends AbstractRetryablePredicate {
   private boolean retried = false;

   @Override
   public Cut getPredicate() {
      return new Cut();
   }

   @Override
   public boolean evaluate() {
      if (retried) {
         throw CutException.CUT_EXCEPTION;
      }
      retried = true;
      return true;
   }
}