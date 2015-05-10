package org.projog.core.function.math;

/* TEST
 %QUERY X is 3 /\ 3
 %ANSWER X=3
 
 %QUERY X is 3 /\ 7
 %ANSWER X=3
 
 %QUERY X is 3 /\ 6
 %ANSWER X=2

 %QUERY X is 3 /\ 8
 %ANSWER X=0

 %QUERY X is 43 /\ 27
 %ANSWER X=11

 %QUERY X is 27 /\ 43
 %ANSWER X=11

 %QUERY X is 43 /\ 0
 %ANSWER X=0

 %QUERY X is 0 /\ 0
 %ANSWER X=0
 */
/**
 * <code>/\</code> - performs bitwise addition.
 */
public final class BitwiseAnd extends AbstractTwoIntegerArgumentsCalculatable {
   @Override
   protected long calculateLong(long n1, long n2) {
      return n1 & n2;
   }
}
