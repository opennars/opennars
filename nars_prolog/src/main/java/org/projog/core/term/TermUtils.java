package org.projog.core.term;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.projog.core.Calculatables;
import org.projog.core.ProjogException;

/**
 * Helper methods for performing common tasks on {@link PTerm} instances.
 */
public final class TermUtils {
   /**
    * A {@link PTerm} array of length 0.
    * <p>
    * Should be used wherever a zero-length {@link PTerm} array is required in order to minimise object creation.
    */
   public static final PTerm[] EMPTY_ARRAY = new PTerm[0];

   /**
    * Private constructor as all methods are static.
    */
   private TermUtils() {
      // do nothing
   }

   /**
    * Returns copies of the specified {link Term}s
    * 
    * @param input {@link PTerm}s to copy
    * @return copies of the specified {link Term}s
    */
   public static PTerm[] copy(final PTerm... input) {
      final int numTerms = input.length;
      final PTerm[] output = new PTerm[numTerms];
      final Map<PVar, PVar> vars = new HashMap<>();
      for (int i = 0; i < numTerms; i++) {
         output[i] = input[i].copy(vars);
      }
      return output;
   }

   /**
    * Backtracks all {@link PTerm}s in the specified array.
    * 
    * @param terms {@link PTerm}s to backtrack
    * @see PTerm#backtrack()
    */
   public static void backtrack(final PTerm[] terms) {
      for (final PTerm t : terms) {
         t.backtrack();
      }
   }

   /**
    * Attempts to unify all corresponding {@link PTerm}s in the specified arrays.
    * <p>
    * <b>Note: If the attempt to unify the corresponding terms is unsuccessful only the terms in {@code queryArgs} will
    * get backtracked.</b>
    * 
    * @param queryArgs terms to unify with {@code consequentArgs}
    * @param consequentArgs terms to unify with {@code queryArgs}
    * @return {@code true} if the attempt to unify all corresponding terms was successful
    */
   public static boolean unify(final PTerm[] queryArgs, final PTerm[] consequentArgs) {
      for (int i = 0; i < queryArgs.length; i++) {
         if (!consequentArgs[i].unify(queryArgs[i])) {
            for (int j = 0; j < i; j++) {
               queryArgs[j].backtrack();
            }
            return false;
         }
      }
      return true;
   }

   /**
    * Returns all {@link PVar}s contained in the specified term.
    * 
    * @param argument the term to find variables for
    * @return all {@link PVar}s contained in the specified term.
    */
   public static Set<PVar> getAllVariablesInTerm(final PTerm argument) {
      final Set<PVar> variables = new LinkedHashSet<>();
      getAllVariablesInTerm(argument, variables);
      return variables;
   }

   private static void getAllVariablesInTerm(final PTerm argument, final Set<PVar> variables) {
      if (argument.constant()) {
         // ignore
      } else if (argument.type() == PrologOperator.NAMED_VARIABLE) {
         variables.add((PVar) argument);
      } else {
         for (int i = 0; i < argument.length(); i++) {
            getAllVariablesInTerm(argument.term(i), variables);
         }
      }
   }

   /**
    * Return the {@link Numeric} represented by the specified {@link PTerm}.
    * 
    * @param t the term representing a {@link Numeric}
    * @return the {@link Numeric} represented by the specified {@link PTerm}
    * @throws ProjogException if the specified {@link PTerm} does not represent a {@link Numeric}
    */
   public static Numeric castToNumeric(final PTerm t) {
      if (t.type().isNumeric()) {
         return (Numeric) t.get();
      } else {
         throw new ProjogException("Expected Numeric but got: " + t.type() + " with value: " + t);
      }
   }

   /**
    * Returns the integer value of the {@link Numeric} represented by the specified {@link PTerm}.
    * 
    * @param t the term representing a {@link Numeric}
    * @return the {@code int} value represented by {@code t}
    * @throws ProjogException if the specified {@link PTerm} cannot be represented as an {@code int}.
    */
   public static int toInt(final PTerm t) {
      Numeric n = castToNumeric(t);
      long l = n.getLong();
      if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
         throw new ProjogException("Value cannot be cast to an int without losing precision: " + l);
      }
      return (int) l;
   }

   /**
    * Return the long value represented by the specified term.
    * 
    * @param t the term representing a long value
    * @return the {@code long} value represented by {@code t}
    * @throws ProjogException if the specified {@link PTerm} does not represent a term of type {@link PrologOperator#INTEGER}
    */
   public static long toLong(final Calculatables calculatables, final PTerm t) {
      final Numeric n = calculatables.getNumeric(t);
      if (n.type() == PrologOperator.INTEGER) {
         return n.getLong();
      } else {
         throw new ProjogException("Expected integer but got: " + n.type() + " with value: " + n);
      }
   }

   /**
    * Return the name of the {@link PAtom} represented by the specified {@link PAtom}.
    * 
    * @param t the term representing an {@link PAtom}
    * @return the name of {@link PAtom} represented by the specified {@link PTerm}
    * @throws ProjogException if the specified {@link PTerm} does not represent an {@link PAtom}
    */
   public static String getAtomName(final PTerm t) {
      if (t.type() != PrologOperator.ATOM) {
         throw new ProjogException("Expected an atom but got: " + t.type() + " with value: " + t);
      }
      return t.getName();
   }

   public static PVar createAnonymousVariable() {
      return new PVar("_");
   }
}