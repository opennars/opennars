package org.projog.core.function.math;

/* TEST
 %QUERY X is 13 >> 0
 %ANSWER X=13

 %QUERY X is 13 >> 1
 %ANSWER X=6
 
 %QUERY X is 13 >> 2
 %ANSWER X=3
 
 %QUERY X is 13 >> 3
 %ANSWER X=1
 
 %QUERY X is 13 >> 4
 %ANSWER X=0
 
 %QUERY X is 13 >> -1
 %ANSWER X=0
 */
/**
 * <code>&gt;&gt;</code> - right shift bits.
 */
public final class ShiftRight extends AbstractTwoIntegerArgumentsCalculatable {
   @Override
   protected long calculateLong(long n1, long n2) {
      return n1 >> n2;
   }
}
