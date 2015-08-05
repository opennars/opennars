package org.projog.core;

import org.projog.core.udp.interpreter.InterpretedUserDefinedPredicate;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception that provides information on an error within the Projog environment.
 * <p>
 * Maintains a collection of all {@link org.projog.core.udp.interpreter.InterpretedUserDefinedPredicate} instances that
 * form the exception's stack trace.
 */
public class ProjogException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   private final List<InterpretedUserDefinedPredicate> interpretedUserDefinedPredicates = new ArrayList<>();

   public ProjogException(String message) {
      super(message, null);
   }

   public ProjogException(String message, Throwable throwable) {
      super(message, throwable);
   }

   public void addUserDefinedPredicate(InterpretedUserDefinedPredicate userDefinedPredicate) {
      interpretedUserDefinedPredicates.add(userDefinedPredicate);
   }

   public List<InterpretedUserDefinedPredicate> getInterpretedUserDefinedPredicates() {
      return interpretedUserDefinedPredicates;
   }
}