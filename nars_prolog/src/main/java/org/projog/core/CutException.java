package org.projog.core;

import org.projog.core.term.PTerm;

/**
 * Exception thrown when the evaluation of a rule backtracks to a cut.
 * 
 * @see org.projog.core.function.flow.Cut
 * @see org.projog.core.udp.interpreter.InterpretedUserDefinedPredicate
 */
public final class CutException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   /**
    * Singleton instance.
    * <p>
    * Reuse a single instance to avoid the stack trace generation overhead of creating a new exception each time. The
    * {@code CutException} is specifically used for control flow in
    * {@link org.projog.core.udp.interpreter.InterpretedUserDefinedPredicate#evaluate(PTerm[])} and it's stack trace is
    * not required.
    */
   public static final CutException CUT_EXCEPTION = new CutException();

   /**
    * Private constructor to force use of {@link #CUT_EXCEPTION}
    */
   private CutException() {
      // do nothing
   }
}